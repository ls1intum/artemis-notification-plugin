package de.tum.in.www1.jenkins.notifications.model;

import com.sun.xml.bind.v2.ContextFactory;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class TestsuiteTest {

    @Test
    void testFlattenNestedSuiteSuccessful() throws Exception {
        Testsuite input = loadTestSuite(Paths.get("nested_successful.xml"));
        assertEquals(2, input.getTestSuites().size());

        Testsuite flattened = input.flatten();
        assertNull(flattened.getTestSuites());

        assertEquals(12, flattened.getTests());
        assertEquals(12, flattened.getTestCases().size());
        assertEquals(0, flattened.getErrors());
        assertEquals(0, flattened.getFailures());
    }

    @Test
    void testFlattenBuildTestCaseNames() throws Exception {
        Testsuite testSuite = loadTestSuite(Paths.get("nested_successful.xml")).flatten();

        List<String> expectedTestCaseNames = new ArrayList<>();
        expectedTestCaseNames.add("Properties.Checked by SmallCheck.Testing filtering in A");
        expectedTestCaseNames.add("Testing selectAndReflectA (0,0) []");

        List<String> actualTestCaseNames = testSuite.getTestCases().stream().map(TestCase::getName).collect(Collectors.toList());

        for (String testCaseName : expectedTestCaseNames) {
            Optional<String> testCase = actualTestCaseNames.stream().filter(testCaseName::equals).findFirst();
            assertTrue(testCase.isPresent(), String.format("Did not find test case '%s' in %s", testCaseName, actualTestCaseNames));
        }
    }

    @Test
    void testFlattenNestedSuiteWithFailures() throws Exception {
        Testsuite input = loadTestSuite(Paths.get("nested_with_failures.xml"));
        assertEquals(2, input.getTestSuites().size());

        Testsuite flattened = input.flatten();
        assertNull(flattened.getTestSuites());

        assertEquals(12, flattened.getTests());
        assertEquals(12, flattened.getTestCases().size());
        assertEquals(2, flattened.getFailures());
        assertEquals(1, flattened.getErrors());
    }

    private Testsuite loadTestSuite(final Path reportXml) throws JAXBException {
        Path resourcePath = new File("testsuite_examples").toPath().resolve(reportXml);
        URL resource = getClass().getClassLoader().getResource(resourcePath.toString());

        final JAXBContext context = createJAXBContext();
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        return (Testsuite) unmarshaller.unmarshal(resource);
    }

    private JAXBContext createJAXBContext() throws JAXBException {
        return ContextFactory.createContext(ObjectFactory.class.getPackage().getName(), ObjectFactory.class.getClassLoader(), null);
    }
}
