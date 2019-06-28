package org.nervos.ckb.jsonrpcTest;
import org.nervos.ckb.framework.system.CKBSystem;
import org.nervos.ckb.util.NetworkUtils;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import com.alibaba.fastjson.JSONObject;
import org.nervos.ckb.util.HttpUtils;
import org.testng.Assert;
import org.testng.annotations.*;

public class IndexLockHashTest extends RPCTestBase {

    private int idlePort = NetworkUtils.getIdlePort();
    private CKBSystem indexLockHashCkbSystem;
    private String dockerName;
    private String ckbDockerImageTagName;
    private String rpcURL = "http://127.0.0.1:" + idlePort;

    @BeforeClass
    public void initIndexLockHashEnv() throws InterruptedException {
        // start a local chain
        indexLockHashCkbSystem = new CKBSystem();
        dockerName = indexLockHashCkbSystem.getDockerName();
        ckbDockerImageTagName = indexLockHashCkbSystem.getCkbDockerImageTagName();
        indexLockHashCkbSystem.enableDebug();
        indexLockHashCkbSystem.cleanEnv();

        // run a idle container
        indexLockHashCkbSystem.init(dockerName, ckbDockerImageTagName, idlePort);

        indexLockHashCkbSystem.ckbInitAddIndexerRun();
        indexLockHashCkbSystem.startCKBMiner();

        Thread.sleep(3000);
    }

    @AfterClass
    public void cleanIndexLockHashEnv() {
        indexLockHashCkbSystem.cleanEnv();
    }

        // test case detail: ${TCMS}/testcase-view-1480-1
        @Test(dataProvider = "positiveData")
        public void testIndexLockHashPositive(String positiveData) {
            JSONObject jsonObject = JSONObject
                    .parseObject(HttpUtils.sendJson(rpcURL, positiveData));
            Assert.assertNull(jsonObject.get("error"));
        }

        // test case detail: ${TCMS}/testcase-view-1485-2
        @Test(dataProvider = "positiveNoBlockNumData")
        public void testIndexLockHashNoBlockNumPositive(String positiveData) {
            JSONObject jsonObject = JSONObject
                    .parseObject(HttpUtils.sendJson(rpcURL, positiveData));
            Assert.assertNull(jsonObject.get("error"));
        }
        // test case detail: ${TCMS}/testcase-view-1488-2
        @Test(dataProvider = "positiveBigBlockNumData")
        public void testIndexLockHashBigBlockNumPositive(String positiveData) {
            JSONObject jsonObject = JSONObject
                    .parseObject(HttpUtils.sendJson(rpcURL, positiveData));
            Assert.assertNull(jsonObject.get("error"));
        }

        // test case detail: ${TCMS}/testcase-view-1481-2
        @Test(dataProvider = "positiveReIndexData")
        public void testReIndexLockHashPositive(String positiveData) {
            JSONObject jsonObject = JSONObject
                    .parseObject(HttpUtils.sendJson(rpcURL, positiveData));
            Assert.assertNull(jsonObject.get("error"));
        }


        // test case detail: ${TCMS}/testcase-view-1482-3
        // test case detail: ${TCMS}/testcase-view-1483-3
        // test case detail: ${TCMS}/testcase-view-1484-3
        // test case detail: ${TCMS}/testcase-view-1486-3
        // test case detail: ${TCMS}/testcase-view-1487-3
        // test case detail: ${TCMS}/testcase-view-1489-3
        @Test(dataProvider = "negativeData")
        public void testIndexLockHashNegative(String negativeData) {
            JSONObject jsonObject = JSONObject
                    .parseObject(HttpUtils.sendJson(rpcURL, negativeData));
            printout(negativeData);
            printout(jsonObject.toJSONString());
            Assert.assertNotNull(jsonObject.get("error"));

        }

        @DataProvider
        public Object[][] negativeData() {
            return new Object[][]{
                    {buildJsonrpcRequest("index_lock_hash")},
                    {buildJsonrpcRequest("index_lock_hash", "")},
                    {buildJsonrpcRequest("index_lock_hash", lockHash,"")},
                    {buildJsonrpcRequest("index_lock_hash", lockHash, "1","2")},
                    {buildJsonrpcRequest("index_lock_hash", lockHash+"98", "1000000")},
                    {buildJsonrpcRequest("index_lock_hash",lockHash.substring(0,64)+"66" , "1000000",lockHash)},
            };
        }


        @DataProvider
        public Object[][] positiveData() throws Exception {
            waitForBlockHeight(1, 180, 1);
            return new Object[][]{
                    {buildJsonrpcRequest("index_lock_hash", lockHash, "1")},

            };
        }

        @DataProvider
        public Object[][] positiveNoBlockNumData() throws Exception {
            waitForBlockHeight(1, 180, 1);
            return new Object[][]{
                    {buildJsonrpcRequest("index_lock_hash", lockHash)},
            };
        }

        @DataProvider
        public Object[][] positiveBigBlockNumData() throws Exception {
            waitForBlockHeight(1, 180, 1);
            return new Object[][]{
                    {buildJsonrpcRequest("index_lock_hash", lockHash, "1000000")},

            };
        }

        @DataProvider
        public Object[][] positiveReIndexData() throws Exception {
            waitForBlockHeight(1, 180, 1);
            HttpUtils.sendJson(url, buildJsonrpcRequest("index_lock_hash", lockHash, "1"));
            return new Object[][]{
                    {buildJsonrpcRequest("index_lock_hash", lockHash, "1")},

            };
        }

    }
