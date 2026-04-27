// Archivo de gran autoridad donde se puede añadir opciones de configuracion comunes para todos los modulos o sub proyectos.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}
