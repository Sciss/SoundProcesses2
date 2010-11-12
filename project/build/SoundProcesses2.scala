import xml._
import sbt.{ FileUtilities => FU, _}

/**
 *    @version 0.10, 27-Oct-10
 */
class SoundProcesses2Project( info: ProjectInfo ) extends DefaultProject( info ) {
   val scalaCollider    = "de.sciss" %% "scalacollider" % "0.21"
   val temporalObjects  = "de.sciss" %% "temporalobjects" % "0.16"
   val ccstm            = "edu.stanford.ppl" % "ccstm" % "0.2.2-for-scala-2.8.0-SNAPSHOT"
   val ccstmRepo        = "CCSTM Release Repository at PPL" at "http://ppl.stanford.edu/ccstm/repo-releases"
   val ccstmSnap        = "CCSTM Snapshot Repository at PPL" at "http://ppl.stanford.edu/ccstm/repo-snapshots"
}
