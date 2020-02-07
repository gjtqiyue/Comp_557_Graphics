For the geom node class, I added a cylinder and wing class to draw a cylinder and wings for my character in openGL. The wing object consist of multiple cubes and a sylinder.

For spherical joint, I apply rotation first so that all the rotation is happened in local space, I think this is a better choice for my character.

The character has movable eyes, eye balls, legs, wings and its long tip at the end of the body. The movement of the tip might be subtle in the animation.

I chose the free joint to be the root, located at the center of the upper body.

I also added a camera perspective (glLookAt()) in freejoint class just to view the character from a more clear aspect.