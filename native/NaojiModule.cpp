/**
 * @author sey
 *
 * Version : $Id$
 * This file was generated by Aldebaran Robotics ModuleGenerator
 */

#include <iostream>
#include "alproxy.h"
#include "alptr.h"
#include "albroker.h"

#include <jni.h>       /* where everything is defined */
#include "NaojiModule.hpp"

using namespace AL;
using namespace Naoji;

//______________________________________________
// constructor
//______________________________________________
NaojiModule::NaojiModule(ALPtr<ALBroker> pBroker, const std::string& pName) :
	ALModule(pBroker, pName) {
	setModuleDescription(
			"This an autogenerated module, this description need to be updated.");

	functionName("JVMTestFunction", "NaojiModule", "the JVMTestFunction.");
	setReturn("return", "Returns no value.");
	BIND_METHOD(NaojiModule::jvmTestFunction);

	isOk = true;
}

//______________________________________________
// destructor
//______________________________________________
NaojiModule::~NaojiModule() {

}

//______________________________________________
// version
//______________________________________________
std::string NaojiModule::version() {
	return ALTools_GetVersionString(NaojiModule_VERSION_MAJOR,
			NaojiModule_VERSION_MINOR, "0", "0");
}

/**
 * dataChanged. Called by ALMemory when subcription
 * has been modified.
 * @param pDataName, name of the suscribed data
 * @param pValue, value of the suscribed data
 * @param pMessage, message written by user during suscription
 */
void NaojiModule::dataChanged(const std::string& pDataName,
		const ALValue& pValue, const std::string& pMessage) {

}

/**
 * innerTest
 */
bool NaojiModule::innerTest() {
	bool result = true;
	// put here code dedicaced to autotest this module.
	// return false if fail, success otherwise
	result = isOk;
	return result;
}

/**
 * jvmTestFunction
 */
ALValue NaojiModule::jvmTestFunction() {
	JNIEnv *env; /* pointer to native method interface */
	int res;
	res = jvm->AttachCurrentThread((void**) &env, NULL);

	if (res < 0) {
		printf("error %d\n", res);
		isOk = false;
		return ALValue(-1);
	}
	/* invoke the Main.test method using the JNI */
	jclass cls = env->FindClass("jp/ac/fit/asura/naoji/NaojiModule");
	if (cls == NULL) {
		printf("cls is null\n");
		isOk = false;
		return ALValue(-2);
	}

	jmethodID mid = env->GetStaticMethodID(cls, "testSquare", "(I)I");
	if (mid == NULL) {
		printf("mid is null\n");
		isOk = false;
		return ALValue(-3);
	}

	int squared = env->CallStaticIntMethod(cls, mid, 10);
	jvm->DetachCurrentThread();

	return ALValue(squared);
}

void NaojiModule::initNaojiModule() {
	JNIEnv *env; /* pointer to native method interface */

	JavaVMInitArgs vm_args;
	JavaVMOption options[4];

	int res;

	vm_args.version = JNI_VERSION_1_6;
	vm_args.options = options;
	vm_args.nOptions = 0;
	vm_args.ignoreUnrecognized = false;

	options[vm_args.nOptions++].optionString
			= "-Djava.class.path=/home/root/naoji/classes:."; /* user classes */
	options[vm_args.nOptions++].optionString
			= "-Djava.library.path=/home/root/naoji/lib:."; /* set native library path */
	//	options[vm_args.nOptions++].optionString = "-server";
	options[vm_args.nOptions++].optionString = "-Xms64m";
	options[vm_args.nOptions++].optionString = "-Xshare:off";
	//	options[vm_args.nOptions++].optionString = "-verbose:jni"; /* print JNI-related messages */
	//	options[vm_args.nOptions++].optionString = "-Djava.compiler=NONE"; /* disable JIT */

	/* Note that in the JDK, there is no longer any need to call
	 * JNI_GetDefaultJavaVMInitArgs.
	 */
	res = JNI_CreateJavaVM(&jvm, (void**) &env, &vm_args);

	if (res < 0) {
		printf("error %d\n", res);
		isOk = false;
		return;
	}
	/* invoke the Main.test method using the JNI */
	jclass cls = env->FindClass("jp/ac/fit/asura/naoji/NaojiModule");
	if (cls == NULL) {
		printf("cls is null\n");
		isOk = false;
		return;
	}

	jmethodID mid = env->GetStaticMethodID(cls, "test", "()V");
	if (mid == NULL) {
		printf("mid is null\n");
		isOk = false;
		return;
	}
	env->CallStaticVoidMethod(cls, mid);
	jvm->DetachCurrentThread();
}

void NaojiModule::exitNaojiModule() {
	/* We are done. */
	jvm->DestroyJavaVM();
}
