/**
 * 
 */
package es.us.isa.restest.searchbased;

import es.us.isa.restest.testcases.TestCase;
import es.us.isa.restest.testcases.TestResult;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.solution.impl.AbstractGenericSolution;

import com.google.common.collect.Maps;

public class RestfulAPITestSuiteSolution extends AbstractGenericSolution<TestCase,RestfulAPITestSuiteGenerationProblem>{

    private Map<TestCase,TestResult> testResults;
    
    public RestfulAPITestSuiteSolution(RestfulAPITestSuiteGenerationProblem problem) {
        super(problem);
        this.testResults=new HashMap<>();
    }    
    
    @Override
    public String getVariableValueString(int i) {
        return getVariable(i).toString();
    }

    @Override
    public RestfulAPITestSuiteSolution copy() {
    	RestfulAPITestSuiteSolution result=new RestfulAPITestSuiteSolution(this.problem);
    	for(int i=0;i<this.getNumberOfVariables();i++) {
    		result.setVariable(i, copyTestCase(this.getVariable(i)));
    		result.testResults.put(result.getVariable(i), copyTestResult(testResults.get(getVariable(i))));
    	}
		return result;
    	
    }
        
    private TestResult copyTestResult(TestResult testResult) {
		return new TestResult(testResult);
	}

	private TestCase copyTestCase(TestCase variable) {		
		return new TestCase(variable);
	}

	@Override
    public Map<Object, Object> getAttributes() {
        return attributes;
    }

    public RestfulAPITestSuiteGenerationProblem getProblem() {
        return problem;
    }

    public TestCase getVariable(int i) {
        return getVariables().get(i);
    }
    
    public void setVariable(int i, TestCase  tc){
        getVariables().set(i, tc);
    }

    public TestResult getTestResult(TestCase testCase) {
        return testResults.get(testCase);
    }

    public void addTestResults(Map<TestCase, TestResult> results) {
        testResults.putAll(results);
    }
    
    public Collection<TestResult> getTestResults() {
    	return testResults.values();
    }
    

    
}