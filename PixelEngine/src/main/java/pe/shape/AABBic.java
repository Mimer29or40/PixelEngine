package pe.shape;

import org.joml.Vector2ic;

public interface AABBic extends Shape2ic
{
    Vector2ic size();
    
    int width();
    
    int height();
    
    Vector2ic min();
    
    int minX();
    
    int minY();
    
    Vector2ic max();
    
    int maxX();
    
    int maxY();
}
