package jp.mc.ancientred.jointblock.common;

public class TypedLinkedList<T extends TypedLinkable> {
   protected TypedLinkable m_head;
   protected TypedLinkable m_tail;
   protected int m_size = 0;

   public TypedLinkable getFirst() {
      return this.m_head;
   }

   public TypedLinkable getLast() {
      return this.m_tail;
   }

   public TypedLinkable getNext(T current) {
      return current.getNext();
   }

   public TypedLinkable getPrevious(T current) {
      return current.getPrev();
   }

   public int size() {
      return this.m_size;
   }

   public void addFirst(T linkable) {
      linkable.setNext((TypedLinkable)null);
      linkable.setPrev((TypedLinkable)null);
      if (this.m_size == 0) {
         this.m_head = this.m_tail = linkable;
      } else {
         linkable.setNext(this.m_head);
         this.m_head.setPrev(linkable);
         this.m_head = linkable;
      }

      ++this.m_size;
   }

   public void addLast(T linkable) {
      linkable.setNext((TypedLinkable)null);
      linkable.setPrev((TypedLinkable)null);
      if (this.m_size == 0) {
         this.m_head = this.m_tail = linkable;
      } else {
         this.m_tail.setNext(linkable);
         linkable.setPrev(this.m_tail);
         this.m_tail = linkable;
      }

      ++this.m_size;
   }

   public void clearDangerous() {
      this.m_head = this.m_tail = null;
      this.m_size = 0;
   }

   public void clear() {
      if (null != this.m_head) {
         for(TypedLinkable link = this.m_head.getNext(); link != null; link = link.getNext()) {
            TypedLinkable prev = link.getPrev();
            prev.setNext((TypedLinkable)null);
            link.setPrev((TypedLinkable)null);
         }

         this.m_head = this.m_tail = null;
      }

      this.m_size = 0;
   }

   public boolean contains(Object o) {
      for(TypedLinkable link = this.m_head; link != null; link = link.getNext()) {
         if (o.equals(link)) {
            return true;
         }
      }

      return false;
   }

   public TypedLinkable removeFirst() {
      TypedLinkable o = this.m_head;
      if (o == null) {
         return null;
      } else {
         TypedLinkable n = o.getNext();
         o.setNext((TypedLinkable)null);
         if (null != n) {
            n.setPrev((TypedLinkable)null);
         }

         this.m_head = n;
         if (--this.m_size == 0) {
            this.m_tail = null;
         }

         return o;
      }
   }

   public TypedLinkable removeLast() {
      TypedLinkable o = this.m_tail;
      if (o == null) {
         return null;
      } else {
         TypedLinkable prev = o.getPrev();
         o.setPrev((TypedLinkable)null);
         if (null != prev) {
            prev.setNext((TypedLinkable)null);
         }

         this.m_tail = prev;
         if (--this.m_size == 0) {
            this.m_head = null;
         }

         return o;
      }
   }

   public boolean remove(Object o) {
      if (!(o instanceof TypedLinkable)) {
         return false;
      } else {
         TypedLinkable link = (TypedLinkable)o;
         TypedLinkable p = link.getPrev();
         TypedLinkable n = link.getNext();
         if (n == null && p == null) {
            if (o != this.m_head) {
               return false;
            }

            this.m_head = this.m_tail = null;
         } else if (n == null) {
            link.setPrev((TypedLinkable)null);
            p.setNext((TypedLinkable)null);
            this.m_tail = p;
         } else if (p == null) {
            link.setNext((TypedLinkable)null);
            n.setPrev((TypedLinkable)null);
            this.m_head = n;
         } else {
            p.setNext(n);
            n.setPrev(p);
            link.setNext((TypedLinkable)null);
            link.setPrev((TypedLinkable)null);
         }

         --this.m_size;
         return true;
      }
   }

   public void addBefore(T current, T newElement) {
      if (current == this.m_head) {
         this.addFirst(newElement);
      } else if (current == null) {
         this.addLast(newElement);
      } else {
         TypedLinkable p = current.getPrev();
         newElement.setNext(current);
         p.setNext(newElement);
         newElement.setPrev(p);
         current.setPrev(newElement);
         ++this.m_size;
      }

   }

   public void addAfter(T current, T newElement) {
      if (current == this.m_tail) {
         this.addLast(newElement);
      } else if (current == null) {
         this.addFirst(newElement);
      } else {
         TypedLinkable n = current.getNext();
         newElement.setPrev(current);
         newElement.setNext(n);
         current.setNext(newElement);
         n.setPrev(newElement);
         ++this.m_size;
      }

   }
}
