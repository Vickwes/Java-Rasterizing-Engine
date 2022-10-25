inline float matrixVectorMul(__global float* resultVector,
    __global float* matrixA,
    __global float* vectorB, 
    int width_A)
{
    int tx = get_global_id(0); 

    float value = 0;
    for (unsigned int k = 0; k < width_A; ++k) {
        value += matrixA[tx * width_A + k] * vectorB[k];
    }

    resultVector[tx] = value;
}
__kernel void
sampleKernel(__global float *x,
             __global float *y,
             __global float *z
             //const float16 projMatrix,
             //const float16 worldMatrix
             )
{

    int gid = get_global_id(0);
    
    /*x[gid] = 200.0f;
    y[gid] = 200.0f;
    z[gid] = 200.0f;*/
}