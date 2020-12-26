/*
out vec4 vColor;
in vec2 vTexCoord;
uniform sampler2D uTexSampler;
*/
uniform float uParameter[5];

// 圆形 [圆心横坐标，圆心纵坐标，半径，透明度]
void draw_circle(float centerX,float centerY,float radius) {
    float x = gl_FragCoord.x - centerX;
    float y = gl_FragCoord.y - centerY;
    if (x * x + y * y < radius * radius) {
        vColor = texture(uTexSampler, vTexCoord);
    } else {
        discard;
    }
}
// 马赛克 [纹理的长度，纹理的高度，马赛克大小，透明度]
void draw_mosaic(float texWidth, float texHeight, float mosaicSize) {
    if (mosaicSize <= 1.0) {
        vColor = texture(uTexSampler, vTexCoord);
    } else {
        //当前点对应在纹理中的位置
        vec2 pointXY = vec2(vTexCoord.x * texWidth, vTexCoord.y * texHeight);
        //找到此店对应马赛克的起点
        vec2 mosaicXY = vec2(floor(pointXY.x / mosaicSize) * mosaicSize, floor(pointXY.y / mosaicSize) * mosaicSize);
        //转换坐标
        vec2 mosaicUV = vec2(mosaicXY.x / texWidth, mosaicXY.y / texHeight);

        vColor = texture(uTexSampler, mosaicUV);
    }
}
// 裁剪 [起点横坐标，起点纵左边，终点横坐标，终点纵左边]
void draw_cut_off(float startX, float endX, float startY, float endY) {
    if (gl_FragCoord.x < startX || gl_FragCoord.x > endX ||
        gl_FragCoord.y < startY || gl_FragCoord.y > endY) {
        discard;
    } else {
        vColor = texture(uTexSampler, vTexCoord);
    }
}
// 横向百叶窗 [分段长度，显示长度]
void draw_hor_shutter(float blockSize, float showSize) {
    if (mod(floor(gl_FragCoord.x), floor(blockSize)) > floor(showSize)) {
        discard;
    } else {
        vColor = texture(uTexSampler, vTexCoord);
    }
}
// 纵向百叶窗 [分段长度，显示长度]
void draw_ver_shutter(float blockSize, float showSize) {
    if (mod(floor(gl_FragCoord.y), floor(blockSize)) > floor(showSize)) {
        discard;
    } else {
        vColor = texture(uTexSampler, vTexCoord);
    }
}
// 方框百叶窗 [分段长度，显示长度]
void draw_hv_shutter(float blockSize, float showSize) {
    if (mod(floor(gl_FragCoord.x), floor(blockSize)) > floor(showSize) ||
        mod(floor(gl_FragCoord.y), floor(blockSize)) > floor(showSize)) {
        discard;
    } else {
        vColor = texture(uTexSampler, vTexCoord);
    }
}
// 去掉高亮度 [ 阀值，-，-，-]
void draw_threshold(float threshold) {
    vec4 color = texture(uTexSampler, vTexCoord);
    float bright = .30 * color.r + .59 * color.g + .11 * color.b;
    if (bright > threshold) {
        discard;
    } else {
        vColor = color;
    }
}
// 设置透明度
void draw_alpha(float alpha) {
    vColor = texture(uTexSampler, vTexCoord);
    vColor.a = alpha;
}

void main() {
    if (uParameter[0] < 1.0) {
        draw_circle(uParameter[1],uParameter[2],uParameter[3]);
    } else if (uParameter[0] < 2.0) {
        draw_mosaic(uParameter[1],uParameter[2],uParameter[3]);
    } else if (uParameter[0] < 3.0) {
        draw_cut_off(uParameter[1],uParameter[2],uParameter[3],uParameter[4]);
    } else if (uParameter[0] < 4.0) {
        draw_hor_shutter(uParameter[1],uParameter[2]);
    } else if (uParameter[0] < 5.0) {
        draw_ver_shutter(uParameter[1], uParameter[2]);
    } else if (uParameter[0] < 6.0) {
        draw_hv_shutter(uParameter[1],uParameter[2]);
    } else if (uParameter[0] < 7.0) {
        draw_threshold(uParameter[1]);
    } else if (uParameter[0] < 8.0) {
        draw_alpha(uParameter[1]);
    } else {
        vColor = texture(uTexSampler, vTexCoord);
    }
}
