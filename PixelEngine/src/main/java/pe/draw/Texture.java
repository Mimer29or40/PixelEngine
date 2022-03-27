package pe.draw;

import org.jetbrains.annotations.NotNull;

interface Texture<SELF>
{
    SELF texture(@NotNull pe.texture.Texture texture);
}
