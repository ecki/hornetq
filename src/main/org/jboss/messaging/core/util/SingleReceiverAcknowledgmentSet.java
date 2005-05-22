/**
 * JBoss, the OpenSource J2EE WebOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.messaging.core.util;


import org.jboss.messaging.core.Acknowledgment;
import org.jboss.logging.Logger;

import java.util.Set;
import java.util.Collections;
import java.io.Serializable;

/**
 * Contains only one Acknowlegment supposedly generated by only one receiver, so the receiverID
 * is irrelevant and will be consequently ignored.
 *
 * TODO Do I need to synchronize access to a AcknowledgmentSet instance?
 *
 * TODO What happens with positive acknowledge placed in the map and not canceled out?
 *
 * @author <a href="mailto:ovidiu@jboss.org">Ovidiu Feodorov</a>
 * @version <tt>$Revision$</tt>
 */
public class SingleReceiverAcknowledgmentSet  implements AcknowledgmentSet
{
   // Constants -----------------------------------------------------

   public static final Logger log = Logger.getLogger(SingleReceiverAcknowledgmentSet.class);

   // Static --------------------------------------------------------

   // Attributes ----------------------------------------------------

   protected Boolean acknowledgment;
   protected boolean deliveryAttempted;

   // Constructors --------------------------------------------------

   public SingleReceiverAcknowledgmentSet()
   {
      deliveryAttempted = false;
   }

   // Public --------------------------------------------------------

   public void update(Set ackSet)
   {
      if (ackSet == null || ackSet.size() == 0)
      {
         return;
      }

      checkSet(ackSet);

      deliveryAttempted = true;
      Acknowledgment ack = (Acknowledgment)ackSet.iterator().next();
      boolean positive = ack.isPositive();
      if (positive)
      {
         acknowledgment = null;
      }
      else
      {
         // negative acknowledge

         // see if there is a positive acknowledge waiting for us
         if (acknowledgment != null && acknowledgment.booleanValue())
         {
            acknowledgment = null; // acks cancel each other
         }
         else if (acknowledgment == null)
         {
            acknowledgment = Boolean.FALSE;
         }
      }
   }

   public void acknowledge(Serializable receiverID)
   {
      // ignore receiverID

      deliveryAttempted = true;

      if (acknowledgment == null)
      {
         acknowledgment = Boolean.TRUE;
      }
      else if (acknowledgment.booleanValue() == false)
      {
         acknowledgment = null;
      }
   }

   public boolean isDeliveryAttempted()
   {
      return deliveryAttempted;
   }

   public int nackCount()
   {
      if (acknowledgment != null && !acknowledgment.booleanValue())
      {
         return 1;
      }
      return 0;
   }

   /**
    * @return a set possibly containing a generic negative acknowledgment. The receiverID value is
    *         undefined.
    */
   public Set getNACK()
   {
      if (acknowledgment != null && !acknowledgment.booleanValue())
      {
         return Acknowledgment.NACKSet;
      }
      return Collections.EMPTY_SET;
   }


   public int ackCount()
   {
      if (acknowledgment != null && acknowledgment.booleanValue())
      {
         return 1;
      }
      return 0;
   }

   /**
    * @return a set possibly containing a generic positive acknowledgment. The receiverID value is
    *         undefined.
    */
   public Set getACK()
   {
      if (acknowledgment != null && acknowledgment.booleanValue())
      {
         return Acknowledgment.ACKSet;
      }
      return Collections.EMPTY_SET;
   }


   public int size()
   {
      return acknowledgment == null ? 0 : 1;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   private void checkSet(Set s)
   {
      if (s.size() > 1)
      {
         log.warn("Undefined behavior when using sets containing more than one element");
      }
   }

   // Inner classes -------------------------------------------------
}


