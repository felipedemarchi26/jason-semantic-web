!start.

+!start
   <- .print(init);
      !!g(9)[hard_deadline(7000)]; // creates a new intention for g with deadline of 7 seconds
      !g(4)[hard_deadline(3000)];  // creates a subgoal with deadline of 3 seconds
      !g(1)[hard_deadline(2000)];
      .print(end1);
      !g(1);
      !g(1);
      !g(1);
      !g(1);
      !g(1);
      .print(end2).

+!g(V) <- .wait(V*1000); .print(g);.

-!g(V)[error(ErrorId)] <- .print("failed, ",ErrorId," for g(",V,")").

