package ai.bitflow.comfyui.multi.gateway.cnst

object WbskCnst {
  const val ON_CONNECT = "connected"
  const val ON_TEXT_MSG = "text_message"
  const val ON_BIN_MSG = "binary_message"
  const val ON_ERROR = "error"
  const val ON_PING = "ping"
  const val ON_PONG = "pong"
  const val ON_DISCONNECT = "disconnected"
  const val ON_QUE_ADD = "added_to_queue"
  const val ON_QUE_FULL = "full_of_queue"
}

object TaskStat {
  const val REQUESTED = "requested"
  const val IN_PROGRESS = "in_progress"
  const val FINISHED = "finished"
  const val ERROR = "error"
}

object LaunchEnv {
  const val DEV = "dev"
  const val STAG = "stag"
  const val PROD = "prod"
}


object JwtAuth {
  const val SIGN_IN = "AUTH0001"
}
