#pragma OPENCL EXTENSION cl_khr_fp64: enable

kernel void step1(global const double *img, int nbPixels, global const double *centers, int nbCenters, global int *assignments) {

    int i = get_global_id(0);
    if(i>=nbPixels) {
        return;
    }

    // Initialization : no group (-1), infinite distance
    double d2Min = DBL_MAX;
    int bestCenter = -1;

    // Search the group which is the closest to the color

    for(int j=0; j<nbCenters; j++) {

        // compute the distance.
        // A pixel contains three components r, g and b. A pixel is written (r, g, b)
        // the squared distance is (r1-r2)^2 + (g1-g2)^2 + (b1-b2)^2 for two pixels (r1, g1, b1) and (r2, g2, b2)

        double delta = img[3 * i + 0] - centers[j * 3 +0]; // Red difference
        double d2 = delta * delta;  // square value

        delta = img[3 * i + 1] - centers[j * 3+ 1 ]; // Green
        d2 += delta*delta;

        delta = img[3 * i + 2] - centers[j * 3 + 2]; // Blue
        d2 += delta*delta;

        // If the distance is smaller, keep the center and the minimal distance
        if(d2Min > d2 ) {
            d2Min = d2;
            bestCenter = j;
        }
    }

    assignments[i] = bestCenter;
}


kernel void step2(global const double *img, int nbPixels, global double *centers, int nbCenters, global const int *assignments) {

    int i = get_global_id(0);
    if(i>=nbCenters) {
        return;
    }

    // compute average color using all colors assigned to the group

    double3 sum = (0, 0, 0);
    int nbUsedPixels = 0;


    // Add colors
    for(int j=0; j<nbPixels; j++) {
        if(i==assignments[j]) {
            double3 p;
            p.x = img[3 * j + 0];
            p.y = img[3 * j + 1];
            p.z = img[3 * j + 2];
            sum += p;

            nbUsedPixels++;
        }
    }

    // Divide by the number of colors in the group

    if(nbUsedPixels>0) {
        centers[i*3+0] = sum.x / nbUsedPixels;
        centers[i*3+1] = sum.y / nbUsedPixels;
        centers[i*3+2] = sum.z / nbUsedPixels;
    }
}
