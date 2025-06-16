#version 120

// Входящие текстурные координаты
varying vec4 texCoord;

// Первый входящий буфер (маска)
uniform sampler2D colorMap;

// Основной буфер (текстура глаз)
uniform sampler2D mainMap;

void main() {
    // Получаем цвет основного буфера (глаза)
    vec4 eyeColor = texture2D(mainMap, texCoord.st);
    
    // Если пиксель глаза прозрачен, делаем его таким же в результирующем изображении
    if (eyeColor.a <= 0.01) discard;
    
    // Получаем цвет второго слоя (радужка и блики)
    vec4 pupilAndGlintColor = texture2D(colorMap, texCoord.st);
    
    // Применяем альфу первого слоя поверх остальных слоев
    float finalAlpha = min(pupilAndGlintColor.a * eyeColor.a, 1.0); // Альфа должна быть ограничена сверху значением 1.0
    
    // Вычисляем итоговый цвет, учитывая прозрачность
    vec3 resultColor = mix(vec3(0), pupilAndGlintColor.rgb, finalAlpha);
    
    gl_FragColor = vec4(resultColor, finalAlpha);
}