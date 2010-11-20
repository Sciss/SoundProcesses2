/*
 *  ProcGroupImpl.scala
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

package de.sciss.synth.proc.impl

import collection.immutable.{Set => ISet}
import de.sciss.synth.proc.{PFactory, Proc, ProcGroup, Ctx}

class ProcGroupImpl[ K, P ]( val name: String )( implicit c: Ctx[ K ], p: PFactory[ P ])
extends ProcGroup[ K, P ] with ModelImpl[ K, ProcGroup.Update[ K, P ]] {
   // pero por-que??
   private val procs = c.v[ P, ISet[ Proc[ K, P ]]]( ISet.empty[ Proc[ K, P ]])

   def add( p: Proc[ K, P ])( implicit c: Ctx[ K ]) {
      procs.set( procs.get + p )
      fireUpdate( ProcGroup.ProcAdded( p ))
   }

   def remove( p: Proc[ K, P ])( implicit c: Ctx[ K ]) {
      procs.set( procs.get - p )
      fireUpdate( ProcGroup.ProcRemoved( p ))
   }

   def all( implicit c: Ctx[ K ]) : Traversable[ Proc[ K, P ]] = procs.get( c )
}