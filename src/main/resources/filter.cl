#pragma OPENCL EXTENSION cl_khr_fp64: enable

kernel void xFilter(global const uchar4 *inImg, 
                    global float4 *tmpImage,
                    int width, int height, 
                    global const double *filter, int filterSize
                    ) {

    // i is the abscissa of the pixel to filter. i is between 0 and width-1
    int i = get_global_id(0);

    if(i>=width)
        return;

    int j = get_global_id(1);

    if(j>=height)
        return;

    int halfFilterSize = filterSize/2;

    int x = i-halfFilterSize; 
    int filterIndex = 0;

    double4 sum = (0, 0, 0, 0);

    // Left
    // x may be negative. In this case, some pixels are missing, we are using the pixel on the left of the image.
    for(;x<0; x++, filterIndex++) {
        sum += convert_double4(inImg[j*width + 0]) * filter[filterIndex];
    }

    // Middle
    // All pixels are available
    for(;x<width && filterIndex<filterSize; x++, filterIndex++) {
        sum += convert_double4(inImg[j*width + x]) * filter[filterIndex];
    }

    // Right
    // Eventually, the filter might use some pixels on the right part of the image that are missing.
    // In such case, we are using the pixel on the right of the image.
    for(;filterIndex<filterSize; filterIndex++) {
        sum += convert_double4(inImg[j*width + width-1]) * filter[filterIndex];
    }


    tmpImage[j * width + i] = convert_float4(sum);
}

kernel void yFilter(global const float4 *tmpImg, 
                    global uchar4 *outImage,
                    int width, int height, 
                    global const double *filter, int filterSize
                    ) {
    int i = get_global_id(0);

    if(i>=width)
        return;

    int j = get_global_id(1);

    if(j>=height)
        return;

    int halfFilterSize = filterSize/2;

    int y = j-halfFilterSize; 
    int filterIndex = 0;

    double4 sum = (0, 0, 0, 0);

    for(;y<0; y++, filterIndex++) {
        sum += convert_double4(tmpImg[i]) * filter[filterIndex];
    }

    for(;y<height && filterIndex<filterSize; y++, filterIndex++) {
        sum += convert_double4(tmpImg[y*width + i]) * filter[filterIndex];
    }

    for(;filterIndex<filterSize; filterIndex++) {
        sum += convert_double4(tmpImg[(height-1) * width + i]) * filter[filterIndex];
    }

    outImage[j * width + i] = convert_uchar4(sum);
}
