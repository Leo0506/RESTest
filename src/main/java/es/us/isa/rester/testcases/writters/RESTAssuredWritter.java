package es.us.isa.rester.testcases.writters;

import static org.junit.Assert.fail;

import java.io.FileWriter;
import java.util.Collection;
import java.util.Map.Entry;

import com.atlassian.oai.validator.restassured.SwaggerValidationFilter;

import es.us.isa.rester.specification.OpenAPISpecification;
import es.us.isa.rester.testcases.TestCase;
import io.swagger.models.Response;

/** REST Assured test writer
 * 
 * @author Sergio Segura & Alberto Martin-Lopez
 *
 */
public class RESTAssuredWritter {
	
	
	private boolean OAIValidation = false;
	private boolean logging = false;
	
	public void write(String specPath, String testFilePath, String className, String packageName, String baseURI, Collection<TestCase> testCases) {
		
		// Initializing content
		String contentFile = "";
		
		// Generating imports
		contentFile += generateImports(packageName);
		
		// Generate className
		contentFile += generateClassName(className);
		
		// Generate attributes
		contentFile += generateAttributes(specPath);
		
		// Generate variables to be used.
		contentFile += generateSetUp(baseURI);
		
		// Generate tests
		int ntest=1;
		for(TestCase t: testCases)
			contentFile += generateTest(t,ntest++);
		
		// Close class
		contentFile += "}\n";
		
		//Save to file
		saveToFile(testFilePath,className,contentFile);
		
		/* Test Compile
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		compiler.run(System.in, System.out, System.err, TEST_LOCATION + this.specification.getInfo().getTitle().replaceAll(" ", "") + "Test.java");
		*/
	}

	private String generateImports(String packageName) {
		String content = "";
		
		if (packageName!=null)
			content += "package " + packageName + ";\n\n";
				
		content += "import org.junit.*;\n"
				 + "import io.restassured.RestAssured;\n"
				 + "import io.restassured.response.Response;\n"
				 + "import org.junit.FixMethodOrder;\n"
				 + "import static org.junit.Assert.fail;\n"
				 + "import org.junit.runners.MethodSorters;\n";
		
		// OAIValidation (Optional)
		if (OAIValidation)
			content += 	"import com.atlassian.oai.validator.restassured.SwaggerValidationFilter;\n";
		
		content +="\n";
		
		return content;
	}
	
	private String generateClassName(String className) {
		return "@FixMethodOrder(MethodSorters.NAME_ASCENDING)\n"
			 + "public class " + className + "Test {\n\n";
	}
	
	private String generateAttributes(String specPath) {
		String content = "";
		
		if (OAIValidation)
			content += "\tprivate static final String OAI_JSON_URL = \"" + specPath + "\";\n"
					+  "\tprivate final SwaggerValidationFilter validationFilter = new SwaggerValidationFilter(OAI_JSON_URL);\n";
		
		content += "\n";
		
		return content;
	}
	
	private String generateSetUp(String baseURI) {
		return 	"\t@BeforeClass\n "
			  + "\tpublic static void setUp() {\n"
			  + "\t\tRestAssured.baseURI = " + "\"" + baseURI + "\";\n"
			  +	"\t}\n\n";
	}
	
	private String generateTest(TestCase t, int instance) {
		String content="";
		
		// Generate test method header
		content += generateMethodHeader(t,instance);
		
		// Generate RESTAssured object pointing to the right path
		content += generateRESTAssuredObject(t);
		
		// Generate header parameters
		content += generateHeaderParameters(t);
		
		// Generate query parameters
		content += generateQueryParameters(t);
		
		// Generate path parameters
		content += generatePathParameters(t);
		
		// OAI validation
		if (OAIValidation)
			content += generateOAIValidationFilter();
		
		// Generate HTTP request
		content += generateHTTPRequest(t);
		
		// Generate basic response validation
		//if(!OAIValidation)
			content += generateResponseValidation(t);
		
		// Close test method
		content += "  }\n\n";
		
		return content;
	}


	private String generateMethodHeader(TestCase t, int instance) {
		return "\t@Test\n" +
				"\tpublic void " + removeCharacters(t.getOperationId()) +
				"Test" + instance + "() {\n";
	}

	private String generateRESTAssuredObject(TestCase t) {
		String content = "";
		
		if (OAIValidation)
			content += "\t\ttry {\n";
			
			content += "\t\t\tResponse response = RestAssured\n"
						+ "\t\t\t.given()\n";
			
		if (logging)
			content +="\t\t\t\t.log().all()\n";
			
		return content;
	}
	
	private String generateHeaderParameters(TestCase t) {
		String content = "";
		
		for(Entry<String,String> param: t.getHeaderParameters().entrySet())
			content += "\t\t\t\t.header(\"" + param.getKey() + "\", \"" + param.getValue() + "\")\n";
		
		return content;
	}
	
	private String generateQueryParameters(TestCase t) {
		String content = "";
		
		for(Entry<String,String> param: t.getQueryParameters().entrySet())
			content += "\t\t\t\t.param(\"" + param.getKey() + "\", \"" + param.getValue() + "\")\n";
		
		return content;
	}
	
	private String generatePathParameters(TestCase t) {
		String content = "";
		
		for(Entry<String,String> param: t.getPathParameters().entrySet())
			content += "\t\t\t\t.pathParam(\"" + param.getKey() + "\", \"" + param.getValue() + "\")\n";
		
		return content;
	}
	

	private String generateOAIValidationFilter() {
		return "\t\t\t\t.filter(validationFilter)\n";
	}
	
	private String generateHTTPRequest(TestCase t) {
		String content = "\t\t\t.when()\n" +
						 "\t\t\t\t." + t.getMethod().name().toLowerCase() + "(\"" + t.getPath() + "\");\n";
		
		// Create response log
		if (logging)
			content += "\n\t\t\tresponse.then().log().all();\n";
			
		
//		if (OAIValidation)
//			content += "\t\t} catch (RuntimeException ex) {\n"
//					+  "\t\t\tSystem.err.println(\"Validation results: \" + ex.getMessage());\n"
//					+  "\t\t\tfail(\"Validation failed\");\n"
//					+	"\t\t}\n";
		
		//content += "\n";
		
		return content;
	}
	
	
	private String generateResponseValidation(TestCase t) {
		String content = "";
		String expectedStatusCode = null;
		boolean thereIsDefault = false;

		// Get status code of the expected response
		for (Entry<String, Response> response: t.getExpectedOutputs().entrySet()) {
			if (response.getValue().equals(t.getExpectedSuccessfulOutput())) {
				expectedStatusCode = response.getKey();
			}
			// If there is a default response, use it if the expected status code is not found
			if (response.getKey().equals("default")) {
				thereIsDefault = true;
			}
		}

		if (expectedStatusCode == null && !thereIsDefault) {
			//TODO: change exception type to the most suitable one
			throw new NullPointerException("The expected status code for this test case is not included among the possible response codes for this operation");
		}

		// Assert status code only if it was found among possible status codes. Otherwise, only JSON structure will be validated
		if (expectedStatusCode != null) {
			content = "\t\t\tresponse.then().statusCode("
					+ expectedStatusCode
					+ ");\n";
		}


		content += "\t\t} catch (RuntimeException ex) {\n"
				+  "\t\t\tSystem.err.println(\"Validation results: \" + ex.getMessage());\n"
				+  "\t\t\tfail(\"Validation failed\");\n"
				+	"\t\t}\n";

		return content;

		/*String content = "\t\tswitch(response.getStatusCode()) {\n";
		boolean hasDefaultCase = false;
		
		for(Entry<String, Response> response: t.getExpectedOutputs().entrySet()) {
			
			// Default response
			if (response.getKey().equals("default")) {
				content += "\t\t\tdefault:\n";
				hasDefaultCase = true;
			} else		// Specific HTTP code
				content += "\t\tcase " + response.getKey() + ":\n";
			
			
				content += "\t\t\tresponse.then().contentType(\"" + t.getOutputFormat() + "\");\n";
		
				//TODO: JSON validation
				content += "\t\t\tbreak;\n";		
		}
		
		if (!hasDefaultCase)
			content += "\t\tdefault: \n"
					+ "\t\t\tSystem.err.println(\"Unexpected HTTP code: \" + response.getStatusCode());\n"
					+ "\t\t\tfail();\n"
					+ "\t\t\tbreak;\n";
		
		// Close switch sentence
		content += "\t\t}\n";
		
		return content;*/
	}
		
	private void saveToFile(String path, String className, String contentFile) {
		FileWriter testClass = null;
		try {
			testClass = new FileWriter(path + "/" + className + "Test.java");
			testClass.write(contentFile);
			testClass.flush();
			testClass.close();
		} catch(Exception ex) {
			System.err.println("Error writing test file: " + ex.getMessage());
			ex.printStackTrace();
		} 
	}

	private String removeCharacters(String s) {
		return s.replaceAll("[^A-Za-z0-9]", "");
	}

	public boolean OAIValidation() {
		return OAIValidation;
	}

	public void setOAIValidation(boolean oAIValidation) {
		OAIValidation = oAIValidation;
	}

	public boolean isLogging() {
		return logging;
	}

	public void setLogging(boolean logging) {
		this.logging = logging;
	}
}
