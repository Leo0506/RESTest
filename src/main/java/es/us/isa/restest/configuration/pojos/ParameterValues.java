package es.us.isa.restest.configuration.pojos;

import es.us.isa.restest.util.PropertyManager;

import java.util.HashSet;
import java.util.Set;

import static es.us.isa.restest.util.CSVManager.readValues;
import static es.us.isa.restest.util.FileManager.createDir;
import static es.us.isa.restest.util.FileManager.createFileIfNotExists;

public class ParameterValues {

    private String experimentName;
    private String operationId;
    private TestParameter testParameter;
    private Set<String> validValues;
    private Set<String> invalidValues;

    public ParameterValues(String experimentName, String operationId, TestParameter testParameter){
        this.experimentName = experimentName;
        this.operationId = operationId;
        this.testParameter = testParameter;

        // Read from csv (if exists)
        String csvPath = this.getCSVPath();
        createDir(csvPath); // This dir is created if it does not exist

        String validPath = this.getValidCSVPath();
        String invalidPath = this.getInvalidCSVPath();
        createFileIfNotExists(validPath);
        createFileIfNotExists(invalidPath);

        this.validValues = new HashSet<>(readValues(validPath));
        this.invalidValues = new HashSet<>(readValues(invalidPath));

    }

    public String getCSVPath(){
        String csvPath = PropertyManager.readProperty("data.tests.dir") + "/" + this.experimentName + "/validAndInvalidValues/" + this.operationId + "/" + this.testParameter.getName() + "/";
        return  csvPath;
    }

    public String getValidCSVPath(){
        String csvPath = this.getCSVPath();
        String validCSVPath = csvPath + "valid.csv";
        return  validCSVPath;
    }

    public String getInvalidCSVPath(){
        String csvPath = this.getCSVPath();
        String invalidCSVPath = csvPath + "invalid.csv";
        return  invalidCSVPath;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public String getOperationId() {
        return operationId;
    }

    public TestParameter getTestParameter() {
        return testParameter;
    }

    public Set<String> getValidValues() {
        return validValues;
    }

    public Set<String> getInvalidValues() {
        return invalidValues;
    }
}
