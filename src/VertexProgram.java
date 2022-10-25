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
public class VertexProgram
{
    /**
     * The source code of the OpenCL program to execute
     */
    int SIZE;
    int n;
    private String programSource;
    private float[] xArray;
    private float[] yArray;
    private float[] zArray;
    private cl_mem xMem;
    private cl_mem yMem;
    private cl_mem zMem;
    private cl_program program;
    private cl_kernel kernel;
    private cl_command_queue commandQueue;
    private Pointer xPointer;
    private Pointer yPointer;
    private Pointer zPointer;
    private cl_context context;




    public VertexProgram(float[][] inArray){
        // Create input- and output data
        xArray = inArray[0];
        yArray = inArray[1];
        zArray = inArray[2];

        n = xArray.length;
        xPointer = Pointer.to(xArray);
        yPointer = Pointer.to(yArray);
        zPointer = Pointer.to(zArray);
        programSource = readFile("src/vert.cl");
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

        // Allocate the memory objects for the input- and output data
        xMem = clCreateBuffer(context,
                CL_MEM_READ_WRITE | CL_MEM_HOST_PTR,
                Sizeof.cl_float * n, xArray, null);
        yMem = clCreateBuffer(context,
                CL_MEM_READ_WRITE | CL_MEM_HOST_PTR,
                Sizeof.cl_float * n, yPointer, null);
        zMem = clCreateBuffer(context,
                CL_MEM_READ_WRITE | CL_MEM_HOST_PTR,
                Sizeof.cl_float* n, zPointer, null);

        // Create the program from the source code
        program = clCreateProgramWithSource(context,
                1, new String[]{ programSource }, null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        kernel = clCreateKernel(program, "sampleKernel", null);

    }

    public float[][] process()
    {



        // Set the arguments for the kernel
        float[] time = new float[]{(float)System.currentTimeMillis()};
        int a = 0;
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(xMem));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(yMem));
        clSetKernelArg(kernel, a++, Sizeof.cl_mem, Pointer.to(zMem));


        // Set the work-item dimensions
        long global_work_size[] = new long[]{n};

        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
                global_work_size, null, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, xMem, CL_TRUE, 0,
                n * Sizeof.cl_float, xPointer, 0, null, null);
        clEnqueueReadBuffer(commandQueue, yMem, CL_TRUE, 0,
                n * Sizeof.cl_float, yPointer, 0, null, null);
        clEnqueueReadBuffer(commandQueue, zMem, CL_TRUE, 0,
                n * Sizeof.cl_float, zPointer, 0, null, null);

        float[][] outputArray = new float[3][];
        outputArray[0]= xArray;
        outputArray[1]= yArray;
        outputArray[2]= zArray;
        // Process the result
       return outputArray;
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