package com.text.filter;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import java.io.IOException;
import java.util.*;
/*
 * 提取文本中的关键词，是受到网页之间关系PageRank算法启发，利用局部词汇之间关系（共现窗口）对后续关键词进行排序
 */
public class TextRankKeyword
{
    public static final int nKeyword = 10;
 
    static final float d = 0.85f;
  
    static final int max_iter = 200;
    static final float min_diff = 0.001f;

    public TextRankKeyword()
    {
        // jdk bug : Exception in thread "main" java.lang.IllegalArgumentException: Comparison method violates its general contract!
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }

    public String getKeyword(String title, String content)
    {
        List<Term> termList = ToAnalysis.parse(title + content);
//        System.out.println(termList);
        List<String> wordList = new ArrayList<String>();
        for (Term t : termList)
        {
            if (shouldInclude(t))
            {
                wordList.add(t.getName());
            }
        }
//        System.out.println(wordList);
        Map<String, Set<String>> words = new HashMap<String, Set<String>>();
        Queue<String> que = new LinkedList<String>();
        for (String w : wordList)
        {
            if (!words.containsKey(w))
            {
                words.put(w, new HashSet<String>());
            }
            que.offer(w);
            if (que.size() > 5)
            {
                que.poll();
            }

            for (String w1 : que)
            {
                for (String w2 : que)
                {
                    if (w1.equals(w2))
                    {
                        continue;
                    }

                    words.get(w1).add(w2);
                    words.get(w2).add(w1);
                }
            }
        }
//        System.out.println(words);
        Map<String, Float> score = new HashMap<String, Float>();
        for (int i = 0; i < max_iter; ++i)
        {
            Map<String, Float> m = new HashMap<String, Float>();
            float max_diff = 0;
            for (Map.Entry<String, Set<String>> entry : words.entrySet())
            {
                String key = entry.getKey();
                Set<String> value = entry.getValue();
                m.put(key, 1 - d);
                for (String other : value)
                {
                    int size = words.get(other).size();
                    if (key.equals(other) || size == 0) continue;
                    m.put(key, m.get(key) + d / size * (score.get(other) == null ? 0 : score.get(other)));
                }
                max_diff = Math.max(max_diff, Math.abs(m.get(key) - (score.get(key) == null ? 0 : score.get(key))));
            }
            score = m;
            if (max_diff <= min_diff) break;
        }
        List<Map.Entry<String, Float>> entryList = new ArrayList<Map.Entry<String, Float>>(score.entrySet());
        Collections.sort(entryList, new Comparator<Map.Entry<String, Float>>()
        {
            public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2)
            {
                return (o1.getValue() - o2.getValue() > 0 ? -1 : 1);
            }
        });
//        System.out.println(entryList);
        String result = "";
        for (int i = 0; i < nKeyword; ++i)
        {
            result += entryList.get(i).getKey() + ':' + entryList.get(i).getValue() + "#";
        }
        return result;
    }

    public static void main(String[] args) throws IOException
    {
//    	String content = "";
//        FileReader fr=new FileReader("part-m-00000");
//        BufferedReader br=new BufferedReader(fr);
//        String s = br.readLine();
//        while(s!=null){
////            System.out.println(s);
//            content = content + s;
//            s=br.readLine();
//        }
//        br.close();
        String content = "近距感应器：当通话时，我们脸部会很接近显示屏，近距感应器会使显示屏停用，这是为了节省电力和防止用户的耳朵和脸部造成无意中的输入";
        System.out.println(new TextRankKeyword().getKeyword("", content));

    }

    class Document
    {
        Map<String, Set<String>> words;
        Map<String, Float> score;
    }

  
    public boolean shouldInclude(Term term)
    {
        if (
                term.getNatrue().natureStr.startsWith("n") ||
                term.getNatrue().natureStr.startsWith("v") ||
                term.getNatrue().natureStr.startsWith("d") ||
                term.getNatrue().natureStr.startsWith("a")
                )
        {
//            if (!StopWordDictionary.contains(term.getName()))
//            {
                return true;
//            }
        }

        return false;
    }
}
