package globalcache;
import com.ibm.broker.config.common.Base64;
import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbGlobalMap;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;

public class AddConfigKey_SaveConfigDetails extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = this.getOutputTerminal("out");
		MbMessage inMessage = inAssembly.getMessage();
		MbMessage outMessage = new MbMessage(inMessage);
		MbMessageAssembly outAssembly = null;
		outAssembly = new MbMessageAssembly(inAssembly, outMessage);
		
		MbElement env = inAssembly.getGlobalEnvironment().getRootElement();
		MbElement variableRef = env.getFirstElementByPath("Variables");
		MbElement keyValue = variableRef.getFirstElementByPath("gcKeyName");

		String sysName = keyValue.getValue().toString();
		
		byte[] inputData = null;
		
		MbElement inputRootElement = inAssembly.getMessage().getRootElement();
		inputData = inputRootElement.getFirstElementByPath("JSON").getFirstElementByPath("Data").toBitstream(null, null, null, 0, 437,0);
		String encText = Base64.encode((byte[]) inputData);
		//InitiatingGlobal Cache
		MbGlobalMap gMap = MbGlobalMap.getGlobalMap();

		//Check for existing Map
		if (gMap.containsKey(sysName)) {
			gMap.update(sysName, (Object) encText);
		} 
		else
		{
			//Inserting map to Global Cache
			gMap.put(sysName, (Object) encText);
		}
		//Propagating output to the next node
		out.propagate(outAssembly);
	}

}
