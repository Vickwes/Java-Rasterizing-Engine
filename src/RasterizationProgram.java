/*
 * JOCL - Java bindings for OpenCL
 *
 * Copyright 2009-2019 Marco Hutter - http://www.jocl.org/
 */

import static org.jocl.CL.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;
import java.util.Arrays;
import javax.vecmath.Point3f;

import org.jocl.*;

import javax.swing.*;

/**
 * A small JOCL sample.
 */
public class RasterizationProgram
{
    /**
     * The source code of the OpenCL program to execute
     */
    private String programSource;
    private cl_program program;
    private cl_kernel kernel;
    private cl_command_queue commandQueue;
    private cl_context context;




    public RasterizationProgram(){
        programSource = readFile("src/rast.cl");
        // The platform, device type and device number
        // that will be used
        final int platformIndex = 0;
        final long deviceType = CL_DEVICE_TYPE_ALL;
        final int deviceIndex = 0;

        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(true);

        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        int numPlatforms = numPlatformsArray[0];


        // Obtain a platform ID
        cl_platform_id platforms[] = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        cl_platform_id platform = platforms[platformIndex];

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Obtain the number of devices for the platform
        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);
        int numDevices = numDevicesArray[0];

        // Obtain a device ID
        cl_device_id devices[] = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        cl_device_id device = devices[deviceIndex];

        // Create a context for the selected device
        context = clCreateContext(
                contextProperties, 1, new cl_device_id[]{device},
                null, null, null);

        // Create a command-queue for the selected device
        cl_queue_properties properties = new cl_queue_properties();
        commandQueue = clCreateCommandQueueWithProperties(
                context, device, properties, null);

        // Create the program from the source code
        program = clCreateProgramWithSource(context,
                1, new String[]{ programSource }, null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        kernel = clCreateKernel(program, "sampleKernel", null);

    }

    public BufferedImage process(float[][] verts, final int size)
    {

        float depthBuffer[] = new float[size*size];
        byte rArray[] = new byte[size*size];
        byte gArray[] = new byte[size*size];
        byte bArray[] = new byte[size*size];
        float xArray[] = verts[0];
        float yArray[] = verts[1];
        float zArray[] = verts[2];
        //Create memory pointers
        Pointer xPointer = Pointer.to(xArray);
        Pointer yPointer = Pointer.to(yArray);
        Pointer zPointer = Pointer.to(zArray);
        Pointer dbPointer = Pointer.to(depthBuffer);
        Pointer rPointer = Pointer.to(rArray);
        Pointer gPointer = Pointer.to(gArray);
        Pointer bPointer = Pointer.to(bArray);

        // Allocate the memory objects for the input- and output data
        cl_mem xMem = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * verts[0].length, xPointer, null);
        cl_mem yMem = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * verts[1].length, yPointer, null);
        cl_mem zMem = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * verts[2].length, zPointer, null);
        cl_mem dbMem = clCreateBuffer(context,
                CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * size*size, dbPointer, null);
        cl_mem rMem = clCreateBuffer(context,
                CL_MEM_WRITE_ONLY,
                Sizeof.cl_char * size*size, null, null);
        cl_mem gMem = clCreateBuffer(context,
                CL_MEM_WRITE_ONLY,
                Sizeof.cl_char * size*size, null, null);
        cl_mem bMem = clCreateBuffer(context,
                CL_MEM_WRITE_ONLY,
                Sizeof.cl_char * size*size, null, null);

        // Set the arguments for the kernel
        float[] time = new float[]{(float)System.currentTimeMillis()};
        int[] sizeArg = new int[]{size};
        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(xMem));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(yMem));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(zMem));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(dbMem));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(rMem));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(gMem));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(bMem));
        clSetKernelArg(kernel, a++, Sizeof.cl_int, Pointer.to(sizeArg));


        // Set the work-item dimensions
        long global_work_size[] = new long[]{1};

        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
                global_work_size, null, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, rMem, CL_TRUE, 0,
                size*size * Sizeof.cl_uchar, rPointer, 0, null, null);
        clEnqueueReadBuffer(commandQueue, gMem, CL_TRUE, 0,
                size*size * Sizeof.cl_uchar, gPointer, 0, null, null);
        clEnqueueReadBuffer(commandQueue, bMem, CL_TRUE, 0,
                size*size * Sizeof.cl_uchar, bPointer, 0, null, null);

        BufferedImage bi = new BufferedImage(size,size,BufferedImage.TYPE_INT_RGB);
        System.out.println(gArray[0]);
        System.out.println(gArray[1]);
        System.out.println(gArray[2]);


        for(int i = 0; i < size; i++){
            for(int j = 0; j < size; j++){
                int index = i * size + j;
                bi.setRGB(j,i,new Color(rArray[index], gArray[index], bArray[index]).getRGB());
            }
        }
        return bi;

    }




    //Helper method to read from kernel.cl
    private static String readFile(String fileName)
    {
        BufferedReader br = null;
        try
        {
            br = new BufferedReader(new FileReader(fileName));
            StringBuilder sb = new StringBuilder();
            String line = null;
            while (true)
            {
                line = br.readLine();
                if (line == null)
                {
                    break;
                }
                sb.append(line+"\n");
            }
            return sb.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            System.exit(1);
            return "";
        }
        finally
        {
            if (br != null)
            {
                try
                {
                    br.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}