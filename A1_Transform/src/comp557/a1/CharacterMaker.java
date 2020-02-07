package comp557.a1;

import javax.vecmath.Vector3d;

public class CharacterMaker {

	static public String name = "CHARACTER NAME - 260728557";
	
	/** 
	 * Creates a character.
	 * @return root DAGNode
	 */
	static public GraphNode create() {
		// TODO: use for testing, and ultimately for creating a character​‌​​​‌‌​​​‌‌​​​‌​​‌‌‌​​‌
		// Here we just return null, which will not be very interesting, so write
		// some code to create a character and return the root node.

		String filename = "a1data/character.xml";
		GraphNode f = CharacterFromXML.load(filename);

		return f;
	}
}
