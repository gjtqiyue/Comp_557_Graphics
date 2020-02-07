Things to notice:

- Originally running on Mac

- I set the focusDistance directly in objective 8 for dolly focus effect, but the desiredFocus remain the same. The potentially effect is that when you disable the dollyfocus the focus distance will change based on unchanged focusDesired value and adjust focus plane to the same distance relative to the dollied camera.

- I did not add any constraint to the dolly value to prevent the near plane from getting to close to the focus plane, so the frustum will still shrink if the near plane is too close to the focus plane. The reason is because I think it is assumed that we shouldn't move the near plane to close to the focus plane. 
