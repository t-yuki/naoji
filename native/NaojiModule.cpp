/**
 * @author sey
 *
 * Version : $Id$
 * This file was generated by Aldebaran Robotics ModuleGenerator
 */

#include <iostream>
#include <sstream>
#include <cassert>
#include <boost/current_function.hpp>

#include "alproxy.h"
#include "alptr.h"
#include "albroker.h"

#include <jni.h>       /* where everything is defined */
#include "NaojiModule.hpp"

using namespace AL;
using namespace Naoji;

#define NAOJI_AUTORUN 1

void jassertion_failed(JNIEnv *env, char const * expr, char const * function,
		char const * file, long line) {
	env->ExceptionDescribe();
	std::ostringstream stream;
	stream << file << ":" << line << ": " << function << ": Assertion `"
			<< expr << "' failed.";
	std::string result = stream.str();
	env->FatalError(result.c_str());
}

#define jassert( env, expr) ((expr)? ((void)0) : jassertion_failed(env, #expr, BOOST_CURRENT_FUNCTION, __FILE__, __LINE__))

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

	functionName("Restart", "NaojiModule", "restart JVM and Naoji modules.");
	setReturn("return", "Returns no value.");
	BIND_METHOD(NaojiModule::restartNaojiModule);

	functionName("Reload", "NaojiModule", "reload Naoji modules.");
	setReturn("return", "Returns no value.");
	BIND_METHOD(NaojiModule::reloadNaojiModule);

	isOk = true;
	jvm = NULL;
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
	// Already initialized.
	if (jvm != NULL)
		return;

	JNIEnv *env;
	int res = initJVM(&env);
	if (res < 0) {
		isOk = false;
		cerr << "Initilization failed. code:" << res;
		return;
	}
	assert(jvm != NULL);
	assert(env != NULL);

	initJavaModule(env);

#ifdef NAOJI_AUTORUN
	runJavaModule(env);
#endif

	jvm->DetachCurrentThread();
}

int NaojiModule::initJVM(JNIEnv **env) {
	JavaVMInitArgs vm_args;
	JavaVMOption options[4];

	int res;

	vm_args.version = JNI_VERSION_1_6;
	vm_args.options = options;
	vm_args.nOptions = 0;
	vm_args.ignoreUnrecognized = false;

	/*
	 * Set JVM options.
	 * We can use -Dxxx, -Xxxx, -verbose options.
	 * see http://java.sun.com/javase/ja/6/docs/ja/technotes/guides/jni/spec/invocation.html#wp16334
	 */
	options[vm_args.nOptions++].optionString
			= "-Djava.class.path=.:naoji.jar:modules/lib:modules/lib/naoji.jar"; /* user classes */
	options[vm_args.nOptions++].optionString
			= "-Djava.library.path=.:modules/lib"; /* set native library path */
	options[vm_args.nOptions++].optionString = "-Xms64m";
	options[vm_args.nOptions++].optionString = "-Xshare:off";
	//options[vm_args.nOptions++].optionString = "-verbose:jni"; /* print JNI-related messages */
	//	options[vm_args.nOptions++].optionString = "-Djava.compiler=NONE"; /* disable JIT */

	/* Note that in the JDK, there is no longer any need to call
	 * JNI_GetDefaultJavaVMInitArgs.
	 */
	res = JNI_CreateJavaVM(&jvm, (void**) env, &vm_args);

	if (res < 0) {
		return res;
	}
	return 0;
}

int NaojiModule::initJavaModule(JNIEnv *env) {
	assert(jvm != NULL);

	// Get class object.
	jclass cls = env->FindClass("jp/ac/fit/asura/naoji/NaojiModule");
	jassert(env, cls != NULL);

	// Get constructor method.
	jmethodID mid = env->GetMethodID(cls, "<init>", "(J)V");
	jassert(env, mid != NULL);

	jlong objPtr = reinterpret_cast<jlong> (this);
	jobject obj = env->NewObject(cls, mid, objPtr);
	jassert(env, obj != NULL);

	// Get global reference.
	naojiObj = env->NewGlobalRef(obj);
	jassert(env, obj != NULL);

	jmethodID initMid = env->GetMethodID(cls, "init", "()V");
	jassert(env, initMid != NULL);

	env->CallVoidMethod(obj, initMid);

	return 0;
}

void NaojiModule::runJavaModule(JNIEnv *env) {
	assert(jvm != NULL);
	assert(naojiObj != NULL);

	jclass naojiClass = env->GetObjectClass(naojiObj);

	jmethodID startMid = env->GetMethodID(naojiClass, "start", "()V");
	jassert(env, startMid != NULL);

	env->CallVoidMethod(naojiObj, startMid);
}

void NaojiModule::exitNaojiModule() {
	/* We are done. */
	if (jvm != NULL) {
		exitJavaModule();
		exitJVM();
	}
}

void NaojiModule::exitJavaModule() {
	assert(jvm != NULL);
	assert(naojiObj != NULL);

	JNIEnv *env; /* pointer to native method interface */
	int res;
	res = jvm->AttachCurrentThread((void**) &env, NULL);
	assert(res >= 0);

	jclass naojiClass = env->GetObjectClass(naojiObj);

	jmethodID exitMid = env->GetMethodID(naojiClass, "exit", "()V");
	jassert(env, exitMid != NULL);

	env->CallVoidMethod(naojiObj, exitMid);

	env->DeleteGlobalRef(naojiClass);
	env->DeleteGlobalRef(naojiObj);
	naojiObj = NULL;

	jvm->DetachCurrentThread();
}

void NaojiModule::exitJVM() {
	jvm->DestroyJavaVM();
	jvm = NULL;
}

ALValue NaojiModule::restartNaojiModule() {
	exitNaojiModule();
	initNaojiModule();
	return ALValue(0);
}

ALValue NaojiModule::reloadNaojiModule() {
	// not implemented.
	return ALValue(0);
}
