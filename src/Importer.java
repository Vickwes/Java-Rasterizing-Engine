import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;

public class Importer {
    public static float[][] importOBJ(String fileName) {
        FileReader fr = null;
        try {
            fr = new FileReader(new File("res/"+fileName+".obj"));
        } catch (FileNotFoundException e) {
            System.err.println("Couldn't load OBJ!");
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(fr);
        String line;
        ArrayList<float[]> verts = new ArrayList<float[]>();
        ArrayList<float[]> unsortedNorms = new ArrayList<float[]>();
        ArrayList<float[]> norms = new ArrayList<float[]>();
        ArrayList<Integer> indicies = new ArrayList<Integer>();
        int offset = verts.size();
        try {
            while(true) {
                line = reader.readLine();
                if(line==null) break;
                String[] currentLine = line.split(" ");
                if(line.startsWith("v ")) {
                    verts.add(new float[]{Float.parseFloat(currentLine[1]), -Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3])});
                }else if (line.startsWith("vn ")) {
                    float[] norm = new float[]{Float.parseFloat(currentLine[1]), Float.parseFloat(currentLine[2]), Float.parseFloat(currentLine[3])};
                    unsortedNorms.add(norm);
                }else if(line.startsWith("f ")) {
                    String[] v1 = currentLine[1].split("/");
                    String[] v2 = currentLine[2].split("/");
                    String[] v3 = currentLine[3].split("/");
                    indicies.add(Integer.parseInt(v1[0])-1 + offset);
                    indicies.add(Integer.parseInt(v2[0])-1 + offset);
                    indicies.add(Integer.parseInt(v3[0])-1 + offset);
                    norms.add(unsortedNorms.get(Integer.parseInt(v1[2])-1));
                }
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
        System.out.println(indicies.size());
        float[][] finalVerts = new float[3][indicies.size()];
        for(int i = 0; i < indicies.size(); i++){
            finalVerts[0][i] = verts.get(indicies.get(i))[0];
            finalVerts[1][i] = verts.get(indicies.get(i))[1];
            finalVerts[2][i] = verts.get(indicies.get(i))[2];
        }
        return finalVerts;
    }
}
