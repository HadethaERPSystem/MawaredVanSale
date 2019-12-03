package com.mawared.mawaredvansale.utilities

import java.io.IOException

class ApiException(message: String) : IOException(message)
class NoConnectivityException(message: String) : IOException(message)