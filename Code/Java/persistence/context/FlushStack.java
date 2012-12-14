package persistence.context;

import persistence.graph.Node;
import java.util.Stack;

/**
 * custom Stack with toString
 * 
 * @author weihang chen
 * 
 */
public class FlushStack extends Stack<Node> {

	private static final long serialVersionUID = 8859350618098702778L;

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Flush Stack size of :" + this.size());
		sb.append(" (From top to bottom):\n");
		sb.append("--------------------------------------------\n");
		for (int i = this.elementCount - 1; i >= 0; --i) {
			sb.append("|").append(get(i)).append("\t|\n");
		}
		sb.append("--------------------------------------------");
		return sb.toString();
	}
}
