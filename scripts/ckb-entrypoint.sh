#!/bin/bash
[ "$DEBUG" == 'true' ] && set -x

time=10
docker_image_name=nervos/ckb # hosted at https://hub.docker.com/r/nervos/ckb

if [ $# -gt 3 ];
  then
  docker_name=$2
  CKB_DOCKER_TAG=$3
  CKB_PORT=$4
  miner_docker_name=${docker_name}-miner
else
  docker_name=run-ckb
  CKB_DOCKER_TAG=latest
  CKB_PORT=8114
  miner_docker_name=run-ckb-miner
fi

start() {
  # before ckb 0.9.0: docker run -d -it -p ${CKB_PORT}:8114 --name=${docker_name} ${docker_image_name}:${CKB_DOCKER_TAG} run
  # change workdir to /home/ckb to let "ckb run" have write permission by default
  docker run --rm -w=/home/ckb --entrypoint "/bin/bash" -d -it -p ${CKB_PORT}:8114 --name=${docker_name} ${docker_image_name}:${CKB_DOCKER_TAG} -c "ckb init && ckb run"
  sleep ${time}
  docker run --rm -w=/home/ckb --entrypoint "/bin/bash" -d -it --net=container:${docker_name} --name=${miner_docker_name} ${docker_image_name}:${CKB_DOCKER_TAG} -c "ckb init && ckb miner"
}

stop() {
  docker rm -f $(docker ps -a -q -f "name=${docker_name}")
}

init() {
  docker run --rm -w=/home/ckb --entrypoint "/bin/bash" -d -it -p ${CKB_PORT}:8114 --name=${docker_name} ${docker_image_name}:${CKB_DOCKER_TAG}
}

# run a command within docker container
# e.g.:  $ ./scripts/ckb-entrypoint.sh run run-ckb ckb "ckb --version"
run() {
  local cmd=$1
  test -t 1 && USE_TTY="-it"  # Jenkins executes its jobs not in a TTY
  docker run --entrypoint "/bin/bash" ${USE_TTY} --rm --name=${docker_name} ${docker_image_name}:${CKB_DOCKER_TAG} -c "${cmd}"
}


update() {
 docker pull ${docker_image_name}:${CKB_DOCKER_TAG}
}

usage(){
    echo "      usage: $0 COMMAND  [args...]"
    echo "      Default Commands"
    echo "      start|stop|init|update"
    echo "      --------------------------"
    echo "      Custom parameter Commands"
    echo "      function container-name image-tag ckb-port"
    echo "      start  run-ckb ckb 8114"
    echo "      stop   run-ckb ckb 8114"
    echo "      init   run-ckb ckb 8114"
    echo "      update run-ckb ckb 8114"
    echo "      run    run-ckb ckb COMMAND"

}


if [ $# -lt 1 ];
then
    usage
else
    case $1 in
        start)
            start
            ;;
        stop)
            stop
            ;;
        init)
            init
            ;;
        run)
            run "$4"
            ;;
        update)
            update
            ;;
        help)
            usage
            ;;
        *)
            usage
            ;;
    esac
fi
