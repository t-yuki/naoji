/**
 * @author sey
 *
 * Version : $Id$
 * This file was generated by Aldebaran Robotics ModuleGenerator
 */

#ifndef NaojiModule_H
#define NaojiModule_H
#include "alptr.h"

namespace AL {
class ALBroker;
}

#include <jni.h>       /* where everything is defined */

/**
 * DESCRIBE YOUR CLASS HERE
 */
#define NaojiModule_VERSION_MAJOR "0"
#define NaojiModule_VERSION_MINOR "1"

namespace Naoji {

class NaojiModule: public AL::ALModule {

public:

	/**
	 * Default Constructor.
	 */
	NaojiModule(AL::ALPtr<AL::ALBroker> pBroker, const std::string& pName);

	/**
	 * Destructor.
	 */
	virtual ~NaojiModule();

	/**
	 * version
	 * @return The version number of ALLeds
	 */
	std::string version();

	/**
	 * init
	 */
	void init() {
		initNaojiModule();
	}

	/**
	 * exit
	 */
	void exit() {
		exitNaojiModule();
		ALModule::exit();
	}

	/**
	 * innerTest
	 * @return True is all the tests passed
	 */
	bool innerTest();

	// **************************** BOUND METHODS **********************************
	/* dataChanged. Called by stm when subcription
	 * has been modified.
	 * @param pDataName Name of the suscribed data
	 * @param pValue Value of the suscribed data
	 * @param pMessage Message written by user during subscription
	 */
	void dataChanged(const std::string& pDataName, const ALValue& pValue,
			const std::string& pMessage);

	/**
	 * dummy Function. An autogenerated example method.
	 * @param pMsg Message to show on screen
	 * @param pFoo the function will return this parameter
	 * @return an alvalue\n
	 */
	ALValue dummyFunction(const std::string& pMsg, const ALValue& pFoo);

protected:
	JavaVM *jvm;

private:
	bool isOk;
	void initNaojiModule();
	void exitNaojiModule();
};
}

#endif // NaojiModule_H
