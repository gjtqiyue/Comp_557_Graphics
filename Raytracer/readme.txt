Feature:
1. reflection (0.5 point)
file: TwoSpherePlaneMirror.png
A ball with perfect mirror reflection, sampled with sphere light

2. refraction (0.5 point)
file: TwoSpherePlaneRefrac.png
A ball with refraction, index of 1.6, only one light

3. Path tracing with sphere light (3 points)
file: TwoSpherePlanePathTracing.png
Three different lights at the back of the sphere, rendering four spheres with different setting of materials. It also shows soft shadow with some detail. The path tracing supports area sampling and brdf sampling. It can be terminated based on hard set depth or Russian roulette.

4. novel scene
file: NovelScene.png
A scene that shows path tracing with all implemented materials, it is also the one that is handed in the competition

5.quadric (0.5 point)
file: Quadric.png
A scene that demonstrates hyperboloid and ellipsoid

In the parser, you can specify different settings for path tracing in the render node
you also need to specify type of material when using Mirror and Fresnel(Refracted) material

For sphere light, you may change it in the light node and specify its radius. Due to the importance sampling method, a bigger light will result in more noise with the same amount of samples.

I also tried DOF in the last minute, but didn't manage to make it working. You may find some trace of it in the code since i run out of time to delete it.

I added a few classes, the ones you may want to check out is Integrator.java and BRDF.java, which implements most of the features.