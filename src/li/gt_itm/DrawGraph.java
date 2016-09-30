package li.gt_itm;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.layout.StaticLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import vnreal.network.substrate.SubstrateLink;
import vnreal.network.substrate.SubstrateNetwork;
import vnreal.network.substrate.SubstrateNode;

public class DrawGraph {
	private Graph<SubstrateNode, SubstrateLink> sn;
	public DrawGraph(Graph<SubstrateNode, SubstrateLink> sn){
		this.sn=sn;
	}
	
	public void draw(){
		Transformer<SubstrateNode, Point2D> position = new Transformer<SubstrateNode,Point2D>(){
			@Override
			public Point2D transform(SubstrateNode arg0) {
				Point2D p = new Point2D.Double();
				p.setLocation(arg0.getCoordinateX()*10, (100-arg0.getCoordinateY())*10);
				return p;
			}};
			
		 Layout<SubstrateNode, SubstrateLink> layout = new StaticLayout<SubstrateNode, SubstrateLink>(sn,position,new Dimension(1000,1000));
		 // The BasicVisualizationServer<V,E> is parameterized by the edge types
		 BasicVisualizationServer<SubstrateNode, SubstrateLink> vv =
		 new BasicVisualizationServer<SubstrateNode, SubstrateLink>(layout);
		 vv.setPreferredSize(new Dimension(1000,1000)); //Sets the viewing area size
		 
		 Transformer<SubstrateNode,String> vetexLabel = new Transformer<SubstrateNode,String>(){
			@Override
			public String transform(SubstrateNode arg0) {
				return arg0.getName();
			}
		 };
		 Transformer<SubstrateLink,String> edgeLabel = new Transformer<SubstrateLink,String>(){
			@Override
			public String transform(SubstrateLink arg0) {
				return arg0.getName();
			}
		 };
		 
		 vv.getRenderContext().setVertexLabelTransformer(vetexLabel);
		 vv.getRenderContext().setEdgeLabelTransformer(edgeLabel);
		 
		 JFrame frame = new JFrame("Simple Graph View");
		 frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 frame.getContentPane().add(vv);
		 frame.pack();
		 frame.setVisible(true); 
	}
}
