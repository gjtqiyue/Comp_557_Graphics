#version 330 core

in vec3 colorv;

void main(void) {
	gl_FragColor = vec4( colorv / 255.0f, 1 );
}