package probabilisticEarley;
// This implements a production in a pcfg grammar
//Juanyan Wang
//A20411039

public class Production
{
	float probability;
	String left;
	String right[];
	int dot;
	int start;

	//Production does double duty as a parse tree; this is for that.
	//therefore, has same number of children as right[], one for
	//each; if there is no child there, null is stored instead.
	Production children[];
	//parent is for the linking as well.
	Production parent;

	/**Simple constructor, assumes no children, initializes everybody.*/
	Production()
	{
		probability=0.0f;
		left = "";
		right = null;
		dot = 0;
		start = 0;
		children = null;
		parent = null;
	}

	/**Constructs a production with n right productions.*/
	Production(int n)
	{
		this();
		right = new String[n];
		children = new Production[n];
		for(int i=0;i<n;i++)
		{
			right[i] = null;
			children[i] = null;
		}
	}

	/**Constructs a production with the given right hand side.*/
	Production(String[] rhs)
	{
		this(rhs.length);
		right = rhs;
	}

	/**Copy constructor.*/
	Production(Production p)
	{
		probability = p.probability;
		left = p.left;
		right = p.right;
		dot = p.dot;
		start = p.start;
		children = new Production[p.children.length];
		for(int i=0;i<children.length;i++) {
			children[i] = p.children[i];
		}
	}

	/**This creates a child of the production given its index.
	 * This adds the child to the production and sets the parent for
	 * the newly created child production.
	 * 
	 * @param n the index on the right hand side where the child attaches
	 * @return The newly created child
	 */
	public final Production spawn(int n)
	{
		Production p = new Production();
		p.parent = this;
		children[n] = p;
		return p;
	}

	/**This creates a child of the production given its index.
         * This adds the child to the production and sets the parent for
         * the newly created child production.  The new child production
	 * will be a copy of the production input as a parameter.
         *
         * @param n the index on the right hand side where the child attaches
	 * @param prod the production to copy the child from
         * @return The newly created child
         */
        public final Production spawn(int n, Production prod)
        {
                Production p = new Production(prod);
                p.parent = this;
                children[n] = p;
                return p;
        }
	
	/**This returns true if the given production matches this one.
	 *
	 * The comparison checks for identical productions only, down to the
	 * placement of the dot.
	 * 
	 * @param p The production to compare to.
	 */
	public final boolean equals(Production p)
	{
		if(left != p.left || right.length != p.right.length || dot != p.dot || start != p.start)
			return false;
		for(int i=0;i<right.length;i++)
			if(right[i] != p.right[i])
				return false;
		return true;
	}
	
	public final boolean equalChildren(Production p) {
		if(children.length == p.children.length) {
			for(int i=0;i<children.length;i++) {
				if(!(children[i] == null && p.children[i] == null)) {
					return false;
				}
				if((children[i] != null && p.children[i] != null) 
						&&(!children[i].equals(p.children[i]))) {
					return false;
				}
			}
		}else {
			return false;
		}
		return true;
	}
	
	/**Easy print.
	 */
	public void print()
	{
		System.out.println(this.toString());
	}

	/**Standard toString human-readable output.
	 * Format:
	 * startpos  left-- right1 . right2
	 * with the dot moving about accordingly.
	 */
	public String toString()
	{
		String ret = start+"\t"+left+"->";
                for(int i=0;i<right.length;i++)
                {
                        if(i==dot)
                                ret = ret + "\t.";
                        ret = ret + "\t" + right[i];
                }
                ret = ret + " [";
                for(int i=0;i<children.length;i++) {
                	ret = ret + (children[i]==null?"null":children[i].printRight())+" ";
                }
                ret = ret + "]";
                if(dot == right.length)
                        ret = ret + "\t.";
                return ret;
	}
	
	public String printRight() {
		String r = "";
		for(int i=0;i<right.length;i++)
        {
           r = r + " "+ right[i];
        }
		return r;
	}

	/**This prints a parse, a chain of productions.
	 * TODO: Write this function!
	 */
	public String recursivePrint()
	{
		String s=left;
		if(children.length == 0 || children[0] == null) {
			s = s + "["+right[0]+"]";
		}else {
			for(int i=0;i<children.length;i++) {
				s = s + "["+children[i].recursivePrint()+"]";
			}
		}
		return s;
	}
	
	public float recursiveProb() {
		float prob = 0;
		prob = (float) (prob + Math.log(probability)/Math.log(2)) ;
		if(children[0] != null) {
			for(int i=0;i<children.length;i++) {
				prob = prob + children[i].recursiveProb();
			}
		}
		return prob;
	}
}
