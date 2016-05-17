import GUT.qual.*;

public class Test{
   @Any Object o = new @Rep Object();
   //:: error: (uts.explicit.lost.forbidden)
   @Lost Object foo(){
        return new @Rep Object();
   }
   //:: error: (uts.explicit.vplost.forbidden)
   Object bar(@VPLost Object p){
        return new @Peer Object();
   }
   //:: error: (uts.explicit.vplost.forbidden)
   @VPLost Object gee(){
        return new @Peer Object();
   }
   
   class InnerClass<T extends @VPLost Object>{

   }
}
