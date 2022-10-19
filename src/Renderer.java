import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Renderer extends JComponent {
    private float[] staticVerts;
    VertexProgram vp;
    RasterizationProgram rp;
    public Renderer(){
        staticVerts = new float[]{100,100,100,100,100,100};
        vp = new VertexProgram(staticVerts);


    }

    @Override
    public void paint(Graphics g){
        float[][] processedVerts = vp.process();
        g.setColor(Color.red);
        BufferedImage bi = new BufferedImage(this.size().height, this.size().width,BufferedImage.TYPE_INT_RGB);
        g.drawImage(bi, 0,0,null);

    }

}
