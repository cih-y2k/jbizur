package ee.ut.jbizur.role.bizur;

import ee.ut.jbizur.config.TestConfig;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import utils.RunnerWithExceptionCatcher;

import java.util.Set;

public class BizurNodeFunctionalTest extends BizurNodeTestBase {

    /**
     * Test for sequential set/get operations of a set of keys and values on different nodes.
     */
    @Test
    public void keyValueSetGetTest() {
        int testCount = TestConfig.getKeyValueSetGetTestCount();
        for (int i = 0; i < testCount; i++) {
            String expKey = "tkey" + i;
            String expVal = "tval" + i;
            putExpectedKeyValue(expKey, expVal);

            BizurNode setterNode = getRandomNode();
            Assert.assertTrue(setterNode.set(expKey, expVal));

            BizurNode getterNode = getRandomNode();
            Assert.assertEquals(expVal, getterNode.get(expKey));
        }
    }

    /**
     * Tests for set/get operations at the same time with multiple nodes.
     */
    @Test
    public void keyValueSetGetMultiThreadTest() throws Throwable {
        int testCount = TestConfig.getKeyValueSetGetMultiThreadTestCount();
        RunnerWithExceptionCatcher runner = new RunnerWithExceptionCatcher(testCount);
        for (int i = 0; i < testCount; i++) {
            int finalI = i;
            runner.execute(() -> {
                String expKey = "tkey" + finalI;
                String expVal = "tval" + finalI;
                putExpectedKeyValue(expKey, expVal);

                BizurNode setterNode = getRandomNode();
                Assert.assertTrue(setterNode.set(expKey, expVal));

                BizurNode getterNode = getRandomNode();
                String actVal = getterNode.get(expKey);
                Assert.assertEquals(expVal, actVal);
            });
        }
        runner.awaitCompletion();
        runner.throwAnyCaughtException();
    }

    /**
     * Test for sequential set/get/delete operations of a set of keys and values on different nodes.
     */
    @Test
    public void keyValueDeleteTest() {
        int testCount = TestConfig.getKeyValueDeleteTestCount();
        for (int i = 0; i < testCount; i++) {
            String expKey = "tkey" + i;
            String expVal = "tval" + i;

            BizurNode setterNode = getRandomNode();
            Assert.assertTrue(setterNode.set(expKey, expVal));

            BizurNode getterNode1 = getRandomNode();
            Assert.assertEquals(expVal, getterNode1.get(expKey));

            BizurNode deleterNode = getRandomNode();
            Assert.assertTrue(deleterNode.delete(expKey));

            BizurNode getterNode2 = getRandomNode();
            Assert.assertNull(getterNode2.get(expKey));
        }
    }

    /**
     * Tests for set/get/delete operations at the same time with multiple nodes.
     */
    @Test
    public void keyValueDeleteMultiThreadTest() throws Throwable {
        int testCount = TestConfig.getKeyValueDeleteMultiThreadTestCount();
        RunnerWithExceptionCatcher runner = new RunnerWithExceptionCatcher(testCount);
        for (int i = 0; i < testCount; i++) {
            int finalI = i;
            runner.execute(() -> {
                String expKey = "tkey" + finalI;
                String expVal = "tval" + finalI;
                putExpectedKeyValue(expKey, expVal);

                Assert.assertTrue(getRandomNode().set(expKey, expVal));
                Assert.assertTrue(getRandomNode().delete(expKey));
                Assert.assertNull(getRandomNode().get(expKey));

                removeExpectedKey(expKey);
            });
        }
        runner.awaitCompletion();
        runner.throwAnyCaughtException();
    }

    /**
     * Tests the iterate keys procedure. Inserts key/val pairs to the bucket. And while inserting,
     * iterates over the inserted keys and compares with the expected values.
     */
    @Test
    @Ignore
    public void iterateKeysTest() {
        int keyCount = TestConfig.getIterateKeysTestCount();
        for (int i = 0; i < keyCount; i++) {
            String key = "tkey" + i;
            String val = "tval" + i;

            putExpectedKeyValue(key, val);
            Assert.assertTrue(getRandomNode().set(key, val));

            Set<String> actKeys = getRandomNode().iterateKeys();
            Assert.assertEquals(getExpectedKeySet().size(), actKeys.size());
            for (String expKey : getExpectedKeySet()) {
                Assert.assertEquals(getExpectedValue(expKey), getRandomNode().get(expKey));
            }
            for (String actKey : actKeys) {
                Assert.assertEquals(getExpectedValue(actKey), getRandomNode().get(actKey));
            }
        }
    }
}