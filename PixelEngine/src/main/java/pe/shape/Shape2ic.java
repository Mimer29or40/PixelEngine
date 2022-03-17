package pe.shape;

import org.joml.Vector2ic;

public interface Shape2ic
{
    Vector2ic pos();
    
    int x();
    
    int y();
    
    AABBic aabb();
}
