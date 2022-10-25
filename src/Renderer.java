import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class Renderer extends JComponent {
    private float[][] staticVerts;
    VertexProgram vp;
    RasterizationProgram rp;
    public Renderer(){
        staticVerts = Importer.importOBJ("tri");
        vp = new VertexProgram(staticVerts);
        System.out.println(Arrays.toString(staticVerts[0]));
        rp = new RasterizationProgram();


    }

    @Override
    public void paint(Graphics g){
        float[][] processedVerts = vp.process();
        System.out.println(Arrays.toString(processedVerts[0]));

        /*
        Sample verticies used for testing
        float[][] betterVerts = new float[][]{{11f,-0.9f,0.0f},{0.0f,0.0f,-0.9f},{-5f,-5f,-5f}};

         */
        g.setColor(Color.red);
        g.drawImage(rp.process(processedVerts, 200),0,0,null);


        //BufferedImage bi = new BufferedImage(this.size().height, this.size().width,BufferedImage.TYPE_INT_RGB);
        //g.drawImage(bi, 0,0,null);

    }

}
