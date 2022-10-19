__kernel void
sampleKernel(__global float *x,
             __global float *y,
             __global float *z)
{

    int gid = get_global_id(0);
    x[gid] = 200.0f;
    y[gid] = 200.0f;
    z[gid] = 200.0f;

}