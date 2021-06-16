dataPath = 'map\';        % 路径
dataDir = dir([dataPath '*.txt']); 
AP = [];
AP_MAX = 11;
Cell_MAX = 8;
j=3;
RSS=[];
N = 300;
means = zeros(1, Cell_MAX);
means_2 = zeros(1, Cell_MAX);
sigma = zeros(1, Cell_MAX);
base = 1:256;
base_2 = base.^2;
a = [];
% data = [];
for i = 1:length(dataDir)         
    data = importdata([dataPath dataDir(i).name]); 
    for j = 1:Cell_MAX
        col = zeros(1,256);
        cols = [];
        for k = 1: 256
            if (data.data(j, 2+k) ~= 0)
                M = 3*data.data(j, 2+k);
                a = [round(1.5*randn(1, M)+ (k))];
                col = zeros(1,256);
                for count = 1:length(a)
                    if (a(count) ~= 0)
                        col(a(count)) = col(a(count)) + 1;
                    end
                end
                cols = [cols; col];
            end   
        end
        [a,b] = size(cols);
        onerow = [];
        
        for count = 1:b
%             data.data(j,k+2) = max(cols(:,count));
            onerow = [onerow, max(cols(:,count))];
%             data.data(j,count+2) = max(cols(:,count));
        end
        if data.data(j,2) ~= 0
            data.data(j,2) = sum(onerow);
            data.data(j,3:258) = onerow;
        end
%         for count = 1:length(a)
%             if (a(count) ~= 0)
%                 data.data(j,a(count)) = data.data(j,a(count)) + 1;
%             end
%         end

%         if (data.data(j,2) ~= 0)
%             means(j) = base*data.data(j,3:258)'/data.data(j,2);
%             means_2(j) = base_2*data.data(j,3:258)'/data.data(j,2);
%             sigma(j) = ((means_2(j) - means(j)^2)^0.5);
%     %         figure
%     %         bar(data.data(j,3:258))
%             a = round(sigma(j)*randn(N,1)+ means(j));
%     %         figure
%     %         hist(a);
%             [count, center] = hist(a,1);
%             data.data(j,2) = N;
%             data.data(j,3:258) = zeros(1,256);
%             for count = 1:N
%                 if (a(count) ~= 0)
%                     data.data(j,a(count)) = data.data(j,a(count)) + 1;
%                 end
%             end
%         end
               
%         figure
%         bar(data.data(j,3:258))

        dlmwrite (dataDir(i).name, data.data);
    end
%     a = sum(data.data(1,3:258))
%     a = data(:,2+2*j)';
%     for k = 1:length(a)
%        if a(k)~=-300
%            RSS=[RSS,a(k)];
%        end
%     end
end
% figure
% bar(onerow);
% AP=[AP;RSS];
% store(AP);
% 
% G_fit=[];
% for AP_index = 1:4
%     hist(AP(AP_index,:))
%     m = mean(AP(AP_index,:))
%     sigma = std(AP(AP_index,:))
% 
%     a = sigma*randn(1500,1)+ m;
%     a = round(a)';
%     G_fit = [G_fit;a];
%     % [count, center]=hist(a,10);
%     % prob = count/sum(count);
%     % bar(center,prob)
%     % xlabel('RSS(in dB)')
%     % ylabel('probability')
% end
% stroe(G_fit)
% dlmwrite ('new_data.txt',G_fit);
% 
% 
% 
% 
% 
% function[] = store (A)
%     fid = fopen('new_data.txt','a')   ;
%     [r,c]=size(A);
%     for i=1:r
%         for j=1:c
%         fprintf(fid,'%d',A(i,j));
%         fprintf(fid,'%s',', ');
%         end
%     end
%     fclose(fid); 
% end

