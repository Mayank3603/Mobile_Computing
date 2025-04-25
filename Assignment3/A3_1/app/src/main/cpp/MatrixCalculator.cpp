#include <jni.h>
#include <valarray>

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_example_a3_11_MainActivity_addMatrices(JNIEnv *env, jobject thiz, jint dim, jdoubleArray a, jdoubleArray b) {
    int n = dim * dim;
    jdouble* arrA = env->GetDoubleArrayElements(a, nullptr);
    jdouble* arrB = env->GetDoubleArrayElements(b, nullptr);

    std::valarray<double> va(arrA, n), vb(arrB, n), vc = va + vb;

    jdoubleArray result = env->NewDoubleArray(n);
    env->SetDoubleArrayRegion(result, 0, n, &vc[0]);

    env->ReleaseDoubleArrayElements(a, arrA, 0);
    env->ReleaseDoubleArrayElements(b, arrB, 0);

    return result;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_example_a3_11_MainActivity_subtractMatrices(JNIEnv *env, jobject thiz, jint dim, jdoubleArray a, jdoubleArray b) {
    int n = dim * dim;
    jdouble* arrA = env->GetDoubleArrayElements(a, nullptr);
    jdouble* arrB = env->GetDoubleArrayElements(b, nullptr);

    std::valarray<double> va(arrA, n), vb(arrB, n), vc = va - vb;

    jdoubleArray result = env->NewDoubleArray(n);
    env->SetDoubleArrayRegion(result, 0, n, &vc[0]);

    env->ReleaseDoubleArrayElements(a, arrA, 0);
    env->ReleaseDoubleArrayElements(b, arrB, 0);

    return result;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_example_a3_11_MainActivity_multiplyMatrices(JNIEnv *env, jobject thiz, jint dim, jdoubleArray a, jdoubleArray b) {
    int n = dim * dim;
    jdouble* arrA = env->GetDoubleArrayElements(a, nullptr);
    jdouble* arrB = env->GetDoubleArrayElements(b, nullptr);

    std::valarray<double> va(arrA, n), vb(arrB, n), vc = va * vb;

    jdoubleArray result = env->NewDoubleArray(n);
    env->SetDoubleArrayRegion(result, 0, n, &vc[0]);

    env->ReleaseDoubleArrayElements(a, arrA, 0);
    env->ReleaseDoubleArrayElements(b, arrB, 0);

    return result;
}

extern "C"
JNIEXPORT jdoubleArray JNICALL
Java_com_example_a3_11_MainActivity_divideMatrices(JNIEnv *env, jobject thiz, jint dim, jdoubleArray a, jdoubleArray b) {
    int n = dim * dim;
    jdouble* arrA = env->GetDoubleArrayElements(a, nullptr);
    jdouble* arrB = env->GetDoubleArrayElements(b, nullptr);

    std::valarray<double> va(arrA, n), vb(arrB, n), vc = va / vb;

    jdoubleArray result = env->NewDoubleArray(n);
    env->SetDoubleArrayRegion(result, 0, n, &vc[0]);

    env->ReleaseDoubleArrayElements(a, arrA, 0);
    env->ReleaseDoubleArrayElements(b, arrB, 0);

    return result;
}
