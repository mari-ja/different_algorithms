package diff.algorithms;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import diff.model.AOCFG;
import diff.model.CustomNode;

/*
 * Ferrante, Ottenstein, Warren, 1987
 * 
 * 1. given the post-dominator tree
 * 2. S = {(A,B) from CFG such that B does not postdominate a (in other words B is not an ancestor of A in the postdominator tree)}
 * 3. for each (A,B) from S determine L - the least common ancestor of A and B in the postdominator tree
 *    only two cases are possible: case 1: L is the parent of A or case 2: L is A *    
 * 4. case 1: L = the parent of A
 * 				all nodes in the postdominator tree on the path from L to B, including B, but not L => are control dependent on A
 * 	  case 2: L = A
 * 				all nodes in the postdominator tree on the path from A to B, including A and B => are control dependent on A 
 *  
 */

public class FerranteControlDependences {
	//first value is source node, and the target nodes are stored in the set
	Map<CustomNode, Set<CustomNode>> S = new HashMap();
	Map<CustomNode, Set<CustomNode>> postDominatorTree = new HashMap();
	Map<CustomNode, Integer> dfs = new HashMap();
	int n = 0;
	
	private AOCFG graph;
	
	public FerranteControlDependences(Map<CustomNode, Set<CustomNode>> pdTree, AOCFG graph) {
		this.postDominatorTree = pdTree;	
		this.graph = graph;
	}
	
	public Map getS() {
		return S;
	}
	
	public Map<CustomNode, Set<CustomNode>> determineControlDependence(CustomNode entryNode) {
		calculateS(entryNode);	
		Map<CustomNode, Set<CustomNode>> controldependence = new HashMap<CustomNode, Set<CustomNode>>();
		for(Entry<CustomNode, Set<CustomNode>> entry : S.entrySet()) {
			CustomNode src = (CustomNode) entry.getKey();
			for (Iterator it =((Set<CustomNode>) entry.getValue()).iterator(); it.hasNext();) {
				CustomNode tgt = (CustomNode) it.next();			
				CustomNode L = determineLasA(src, tgt);			
				if (L != null) { //case 1: L is A, find nodes on the path from A to B, including A and B					
					Set nodes = new HashSet();
					findNodesfromAtoB(L, tgt, nodes);
					if (controldependence.containsKey(src)) {
						nodes.addAll((Set)controldependence.get(src));
					}
					controldependence.put(src, nodes);					
				} else { //case 2: L is parent of A, find nodes on the path from L to B, including B, but not L
					L = determineLasParentofA(src, tgt);
					Set nodes = new HashSet();
					findNodesfromLtoB(L, tgt, nodes);
					if (controldependence.containsKey(src)) {
						nodes.addAll((Set)controldependence.get(src));
					}
					controldependence.put(src, nodes);				
				}				
			}			
		}		
		return controldependence;
	}
	
	private void findNodesfromAtoB(CustomNode A, CustomNode B, Set s) {		
		s.add(B);
		for (Entry e : postDominatorTree.entrySet()) {
			if (B.equals(e.getValue())) {
				CustomNode parent = (CustomNode) e.getKey();
				if (!parent.equals(A)) {						
					findNodesfromLtoB(A, parent, s);					
				} else {
					s.add(parent);
				}
			}
		}	
	}
	
	private void findNodesfromLtoB(CustomNode L, CustomNode B, Set s) {		
		s.add(B);
		for (Entry e : postDominatorTree.entrySet()) {
			if (((Set) e.getValue()).contains(B)) {
				CustomNode parent = (CustomNode) e.getKey();
				if (!parent.equals(L)) {						
					findNodesfromLtoB(L, parent, s);					
				}
			}
		}	
	}
	
	private void calculateS(CustomNode entryNode) {
		n++; dfs.put(entryNode, n);	
		Set<CustomNode> nodes = graph.getOutgoingNodes(entryNode);			
		for(CustomNode otherNode : nodes) {
			boolean flag = false;
			if (postDominatorTree.containsKey(otherNode)) {
				Set s_nodes = (Set) postDominatorTree.get(otherNode);
				for (Iterator it = s_nodes.iterator(); it.hasNext();) {
					CustomNode n = (CustomNode) it.next(); 
					if (n.equals(entryNode)) {
						flag = true;
						break;
					}
				}				
			}
			if (!flag) {
				Set tgts;
				if (S.containsKey(entryNode)) {
					tgts = S.get(entryNode);
				} else {
					tgts = new HashSet();
				}
				tgts.add(otherNode);
				S.put(entryNode, tgts);
			}
			if (!dfs.containsKey(otherNode)) {
				calculateS(otherNode);				
			}			
		}			
	}
	
	private CustomNode determineLasA(CustomNode A, CustomNode B) {
		CustomNode L = null;
		boolean flag = false;
		if (postDominatorTree.containsKey(A)) {
			Set s_nodes = (Set) postDominatorTree.get(A);
			for (Iterator it = s_nodes.iterator(); it.hasNext();) {
				CustomNode n = (CustomNode) it.next();			
				if (B.equals(n)) {
					L = A;	
					flag = true;
				}
			}
			
			if (!flag) {
				for (Iterator it = s_nodes.iterator(); it.hasNext();) {
					CustomNode n = (CustomNode) it.next();
					L = determineLasA(n, B);								
				}		
			}
		}
		return L;
	}
	
	private CustomNode determineLasParentofA(CustomNode A, CustomNode B) {
		CustomNode L = null;
		for (Entry entry : postDominatorTree.entrySet()) {
			if (((Set) entry.getValue()).contains(A)) {
				L = (CustomNode) entry.getKey();
				return L;
				}
		}
		return L;	
	}	
}
