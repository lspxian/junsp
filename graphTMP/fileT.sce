f = read("s1-node.txt",-1,4);
x = f(:,3);
y = f(:,4);
plot(x,y,'.');

f2 = read("s1-link.txt",-1,3);
a = f2(:,1);
b = f2(:,2);

for i=1:size(a,"r")
    plot([x(a(i)+1),x(b(i)+1)],[y(a(i)+1),y(b(i)+1)],'-');
end

f = read("s2-node.txt",-1,4);
x = f(:,3)+100;
y = f(:,4);
plot(x,y,'.');

f2 = read("s2-link.txt",-1,3);
a = f2(:,1);
b = f2(:,2);

for i=1:size(a,"r")
    plot([x(a(i)+1),x(b(i)+1)],[y(a(i)+1),y(b(i)+1)],'-');
end
