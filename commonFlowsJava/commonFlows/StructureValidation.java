package commonFlows;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbBroker;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;

public class StructureValidation extends MbJavaComputeNode {
public String sitDirectory;
public String devDirectory;
public String uatDirectory;
public String prodDirectory;
public String environment;

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		environment = MbBroker.getBroker().getName();
		sitDirectory = getUserDefinedAttribute("sit").toString();
		devDirectory = getUserDefinedAttribute("dev").toString();
		uatDirectory = getUserDefinedAttribute("uat").toString();
		prodDirectory = getUserDefinedAttribute("prod").toString();
		MbOutputTerminal out = getOutputTerminal("out");
		MbOutputTerminal alt = getOutputTerminal("alternate");
		MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;
		String jsonSchemaBaseUrl;
		MbElement env = inAssembly.getGlobalEnvironment().getRootElement();
		MbElement variableRef = env.getFirstElementByPath("Variables");
		try {
			// create new message as a copy of the input
			MbMessage outMessage = new MbMessage(inMessage);
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);
			// Add user code below
			if(environment.contains("DEV"))
			jsonSchemaBaseUrl = devDirectory;
			else if(environment.contains("SIT"))
			jsonSchemaBaseUrl = sitDirectory;
			else if((environment.contains("UAT"))||(environment.contains("UAE")))
			jsonSchemaBaseUrl = uatDirectory;
			else if((environment.contains("PROD"))||(environment.contains("PRD")))
			jsonSchemaBaseUrl = prodDirectory;
			else
			jsonSchemaBaseUrl = "C:\\Schemas\\";
			
			MbElement keyValueJsonData = variableRef.getFirstElementByPath("jsonData");
			String jsonDataString = keyValueJsonData.getValue().toString();	
			JSONArray jsonArray = null;
			JSONObject jsonObj = null;
			if(jsonDataString.startsWith("["))
				jsonArray = new JSONArray(jsonDataString);
				else jsonObj = new JSONObject(jsonDataString);
			MbElement keyValueJsonSchemaName = variableRef.getFirstElementByPath("jsonSchemaName");
			String jsonSchemaName = keyValueJsonSchemaName.getValue().toString();
			String jsonSchemaUrl = jsonSchemaBaseUrl + jsonSchemaName + ".json";
			File schemaFile = new File(jsonSchemaUrl);
			Reader reader = new FileReader(jsonSchemaUrl);
			if(schemaFile.exists()){
				JSONTokener jsonTokener = new JSONTokener(reader);
            	JSONObject jsonSchema = new JSONObject(jsonTokener);
            	Schema schema = SchemaLoader.load(jsonSchema);
	            if(jsonArray != null)
	            	schema.validate(jsonArray); 
	            else
	            	schema.validate(jsonObj);
	            if( !schema.equals(null))
	            	variableRef.createElementAsLastChild(0,"isValidJson","true");
		  	}// e if
		} catch (MbException e) {
            // Re-throw to allow Broker handling of MbException
            throw e;
        } catch (RuntimeException e) {
            // Re-throw to allow Broker handling of RuntimeException
        	variableRef.createElementAsLastChild(0,"isValidJson","false");
		} catch (Exception e) {
			System.out.println("Invalid json!!!\n"+e.getMessage()+"\n"+((ValidationException) e).getCausingExceptions());	
			variableRef.createElementAsLastChild(0,"isValidJson","false");
		}
		out.propagate(outAssembly);
	}	
}
