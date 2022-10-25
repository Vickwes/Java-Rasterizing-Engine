inline int toScreenCoords(float x, int size){
    return size / 2 + x * size / 2;
}

__kernel void
sampleKernel(__global float *x,
             __global float *y,
             __global float *z,
             __global float *depth,
             __global uchar *r,
             __global uchar *g,
             __global uchar *b,
             const int size
             )
{
    int gid = get_global_id(0) * 3;
    int x0 = toScreenCoords(x[gid], size);
    int x1 = toScreenCoords(x[gid+1], size);
    int x2 = toScreenCoords(x[gid+2], size);
    int y0 = toScreenCoords(y[gid], size);
    int y1 = toScreenCoords(y[gid+1], size);
    int y2 = toScreenCoords(y[gid+2], size);
    float z0 = z[gid];
    float z1 = z[gid+1];
    float z2 = z[gid+2];
    for(int i = y0; i>y2; i--) {
            
            if(i <= y1)break;
            int pixL = (int) (x0 + (x2  - x0) * (i - y0) / (y2-y0));
            float f = (float)(i - y2) / (y0-y2);
            float depthL = z0 + f * (z2 - z0);
            int pixR = (int) (x1 + (x0  - x1) * (i - y1) / (y0-y1));
            float depthR = z1 + f * (z0 - z1);
            int left = min(pixL, pixR);
            int right = max(pixL, pixR);
            for(int j = left; j > right; j++) {
                float pixDepth = -(depthL + ((j - left) / (right - left) * (depthR - depthL)));
                if(pixDepth < depth[j * size + i]){
                    depth[j*size+i] = pixDepth;
                    r[j*size+i] = 50;
                    g[j*size+i] = 50;
                    b[j*size+i] = 50;
                }
            }
        }
        for(int i = y1; i>y2; i--) {
            int pixL = (int) (x0 + (x2  - x0) * (i - y0) / (y2-y0));
            int pixR = (int) (x1 + (x2  - x1) * (i - y1) / (y2-y1));
            float f = (float)(i - y2) / (y0-y2);
            float depthL = z0 + f * (z2 - z0);
            float depthR = z1 + f * (z2 - z1);
            int left = min(pixL, pixR);
            int right = max(pixL, pixR);
            for(int j = left; j< right; j++) {
                float pixDepth = -(depthL + ((j - left) / (right - left) * (depthR - depthL)));
                if(pixDepth > depth[j * size + i]){
                    depth[j*size+i] = pixDepth;
                    r[j*size+i] = 50;
                    g[j*size+i] = 50;
                    b[j*size+i] = 50;
                }   
            }
        }
}