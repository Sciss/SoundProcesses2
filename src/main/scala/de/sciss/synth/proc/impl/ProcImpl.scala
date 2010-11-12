/*
 *  ProcImpl.scala
 *  (SoundProcesses)
 *
 *  Copyright (c) 2009-2010 Hanns Holger Rutz. All rights reserved.
 *
 *	 This software is free software; you can redistribute it and/or
 *	 modify it under the terms of the GNU General Public License
 *	 as published by the Free Software Foundation; either
 *	 version 2, june 1991 of the License, or (at your option) any later version.
 *
 *	 This software is distributed in the hope that it will be useful,
 *	 but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *	 General Public License for more details.
 *
 *	 You should have received a copy of the GNU General Public
 *	 License (gpl.txt) along with this software; if not, write to the Free Software
 *	 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 *	 For further information, please contact Hanns Holger Rutz at
 *	 contact@sciss.de
 *
 *
 *  Changelog:
 */

package de.sciss.synth.proc
package impl

import collection.immutable.{Queue => IQueue}

class ProcImpl[ C ]( val name: String )( implicit c: Ctx[ C ])
extends Proc[ C ] with ModelImpl[ C, Proc.Update ] {
   override def toString = "Proc(" + name + ")"

   val playing = new SwitchImpl( "playing", false )
   val amp     = new ControllerImpl( "amp", 1.0 )
   val freq    = new ControllerImpl( "freq", 441.0 )

   playing.addListener( new Model.Listener[ C, Boolean ] {
      def updated( v: Boolean )( implicit c: Ctx[ C ]) {
//         println( "PROC : playing now " + v )
         fireUpdate( Proc.Update( playing -> v ))
      }
   })
   amp.addListener( new Model.Listener[ C, Double ] {
      def updated( v: Double )( implicit c: Ctx[ C ]) {
         fireUpdate( Proc.Update( amp -> v ))
      }
   })
   freq.addListener( new Model.Listener[ C, Double ] {
      def updated( v: Double )( implicit c: Ctx[ C ]) {
         fireUpdate( Proc.Update( freq -> v ))
      }
   })

//   private val listeners = c.v( IQueue.empty[ L ])
//
//   def addListener( l: L )( implicit c: Ctx[ C ]) {
//      listeners.transform( _ enqueue l )( txn( c ))
//   }
//
//   def removeListener( l: L )( implicit c: Ctx[ C ]) {
//      listeners.transform( _.filter( _ != l ))( txn( c ))
//   }
}