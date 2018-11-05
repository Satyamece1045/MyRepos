package globalcache;

import com.ibm.broker.config.common.Base64;
import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.MbElement;
import com.ibm.broker.plugin.MbException;
import com.ibm.broker.plugin.MbGlobalMap;
import com.ibm.broker.plugin.MbMessage;
import com.ibm.broker.plugin.MbMessageAssembly;
import com.ibm.broker.plugin.MbOutputTerminal;
import com.ibm.broker.plugin.MbUserException;

public class GetGlobalCache_getGlobalCacheValue extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = this.getOutputTerminal("out");
		MbMessage inMessage = inAssembly.getMessage();
		MbMessageAssembly outAssembly = null;
		try {
			MbMessage outMessage = new MbMessage(inMessage);
			outAssembly = new MbMessageAssembly(inAssembly, outMessage);
			outAssembly.getMessage().getRootElement().delete();
			MbElement env = inAssembly.getGlobalEnvironment().getRootElement();
			MbElement variableRef = env.getFirstElementByPath("Variables");
			MbElement keyValue = variableRef.getFirstElementByPath("gcKeyName");
			String sysName = keyValue.getValue().toString();

			// InitiatingGlobal Cache
			MbGlobalMap gMap = MbGlobalMap.getGlobalMap();			
			// Getting map from Global Cache
			String gcValue = (String) gMap.get((Object) sysName);
			if (gcValue!=null) {
				// Converting the raw data back to byte array
				byte[] decData = Base64.decode((String) gcValue);
				// Prepare Output as JSON object
				outMessage.getRootElement().createElementAsLastChildFromBitstream(
						decData, "JSON", null, null, null, 0, 0, 0);
			}
			else
			{
				variableRef.createElementAsLastChild(0,"GCSTATUS","EMPTY");
				variableRef.createElementAsLastChild(0,"docid","");
				variableRef.createElementAsLastChild(0,"filterString","");	
				variableRef.createElementAsLastChild(0,"DATABASE","config");
				variableRef.createElementAsLastChild(0,"COLLECTION_NAME","esbConfig");
			}				
			
		} catch (MbException e) {
			throw e;
		} catch (RuntimeException e) {
			throw e;
		} catch (Exception e) {
			throw new MbUserException((Object) this, "evaluate()", "", "",
					e.toString(), null);
		}
		out.propagate(outAssembly);
	}
}
