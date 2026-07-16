WITH upserted_book AS (
    INSERT INTO books (title, author, source_filename, content_sha256)
    VALUES (
        'Jewels of the Millionaire Mind',
        'Nilo Ortega',
        'whatsapp-images-jewel-1-choices-2026-07-15',
        encode(digest('jewel-1-choices-visible-image-excerpts-2026-07-15', 'sha256'), 'hex')
    )
    ON CONFLICT (content_sha256) DO UPDATE
    SET title = EXCLUDED.title,
        author = EXCLUDED.author,
        source_filename = EXCLUDED.source_filename
    RETURNING id
)
INSERT INTO book_chunks (book_id, chunk_index, content, token_count)
SELECT upserted_book.id, chunks.chunk_index, chunks.content, chunks.token_count
FROM upserted_book
CROSS JOIN (
    VALUES
        (1, $chunk$
Jewel #1 - Choices
The Power You Carry Everywhere

"Life is a matter of choices, and every choice you make makes you."
- John C. Maxwell, Intentional Living

What is the single force that determines the quality of your life?
$chunk$, 35),
        (2, $chunk$
At first, the question almost feels unfair. Life is complicated - surely everything matters. Circumstances. Opportunity. Family. Health. Money. Timing. Luck. You could list influences forever and still be right.

But when you strip life down to its core - beneath talent, beneath chance, beneath everything you cannot control - one quiet power remains, steady and constant: choice. It is the steering wheel of your future, sometimes invisible, always present. Whether you notice it or not, you are choosing every moment.

You cannot decide the family you were born into, the country you start out in, or the problems the world hands you. But you always have control over what you focus on, what meaning you give to what happens, and how you respond when life pushes back. You may not control the world - but you do control your decisions within it.
$chunk$, 126),
        (3, $chunk$
The Shape of a Life

Every second of your life is shaped by choices - small ones, invisible ones, and sometimes monumental ones. Most choices do not announce themselves as life-changing when they arrive. They slip subtly into ordinary moments: the people you spend time with, what you do with your free hours, how you speak when silence would be easier, whether you quit or try again. These decisions feel harmless in isolation - until they are not. Over time, every choice leaves a fingerprint on your future.

Stephen Covey once captured this truth with piercing simplicity:

"I am not a product of my circumstances.
I am a product of my decisions."

One sentence. A lifetime of consequence.
$chunk$, 112),
        (4, $chunk$
History echoes this truth again and again. Consider Jeff Bezos. Long before anyone imagined Amazon, he was simply an employee - successful on paper, restless inside. In 1994, he came across a report forecasting the explosive growth of the internet. An online bookstore - absurd at the time - captured his imagination.

The internet was still a question mark. A risk. A leap into uncertainty.

Bezos faced the same ancient choice: remain comfortable or step into the unknown. He chose uncertainty. He left his job, packed his car, and drove across the country to Seattle. With a small team and a fragile idea, he built what would become Amazon - named after the largest river in the world to reflect the scale of his vision.

Years later, speaking to graduates, he distilled his journey into a sentence that still lingers:

"I didn't think I'd regret trying and failing.
But I knew I would regret not trying at all."

He then asked questions that reach far beyond career advice:

- Will you follow inertia or passion?
- Will you choose comfort or courage?
- Will you play it safe or take a chance?
- Will you be a critic or a builder?
- Will you be kind, even when it costs you something?

His conclusion was simple and sobering: when you are eighty years old, the most meaningful version of your life story will be the series of choices you made. In the end, we are our choices. Build yourself a great story.
$chunk$, 229),
        (5, $chunk$
My Hardest Choice - The One That Changed Everything

I still remember the moment my life split into two possible futures.

I was in Cuba, sitting alone in a warm room with a rattling fan overhead, staring at the ceiling and thinking, Now what? I could stay where everything was familiar and predictable - comfortable, routine, safe, and slowly numbing. Or I could leave - with no English, no money, no support, and no clear idea how I would survive.

Both paths frightened me.

Staying meant comfort, but also the quiet erosion of potential. Leaving meant possibility, but also pain, uncertainty, and loss. I knew that choosing to leave might mean never returning, never seeing some loved ones again.

That realization broke something open inside me. I doubted myself. I cried alone - not out of weakness, but out of love. Love for the people I might leave behind. Love for the future I hoped to give them.

And still - I chose.

Fear stayed in my stomach, but courage took the wheel. That single decision reshaped everything that followed. It taught me something simple and lasting: action hurts less than regret. Doing something is harder - but better - than standing still. No one can choose your life for you. Only you can. And you must live with what you choose.

From that choice came freedom. From that choice came my children, Alex and Angie - the greatest gifts of my life. And to you, reader, whoever you are: your own future gifts are waiting on the other side of your brave decisions.
$chunk$, 252),
        (6, $chunk$
Your story does not need to look like his - and it shouldn't. You may never build a global company or move across a continent. But the decisions you face will shape your life just as powerfully in your own way.

Choosing Growth Later in Life

Years later - nearing forty - I stood at another crossroads. Two children. Aging parents. A new country. Limited English. I was working as a dental hygienist, earning decent money. Comfortable - internally restless.

Going back to school in 2020 meant competing with twenty-year-olds, studying late nights during a pandemic, squeezing myself into a tiny closet to learn because we had no space. Some days were fueled by exhaustion and doubt. I wondered if I was being reckless or unrealistic.

But again, I chose growth instead of comfort. Persistence instead of excuses.

Today, we live in sunny Florida, and I practice dentistry with pride. The life we enjoy now was planted years ago through invisible choices that demanded patience before they delivered reward.

The Choice I Didn't Make

Not every choice I made was brave. I loved sports - basketball, soccer, baseball - anything competitive. I had the mind of an athlete, the hunger, the discipline. But I fed myself convenient excuses: No one in my family did it. I don't know how. What if I fail?
$chunk$, 202),
        (7, $chunk$
So I didn't choose that path. I followed a traditional one instead. I do not regret becoming a dentist - it gave me stability and purpose - but a part of me still wonders what if? Not with sadness, but with awareness. Unchosen dreams don't disappear. They echo. Sometimes the cost of safety is a lingering regret that visits later in life.

One day, often without warning, you realize something both terrifying and beautiful: no one is choosing for you anymore. Not your parents. Not your teachers. Not your friends. Advice will still come - sometimes wise, sometimes loud - but the hand on the wheel will be yours. That is the moment life stops happening to you and begins asking to be built by you.

You may think choices only matter when they are big - college, careers, relationships. But your future is shaped much earlier, in smaller, hidden moments:

- One more episode or sleep
- Honesty or excuses
- Practice or procrastination
- Gossip or silence
- Kindness or sarcasm
- Studying or scrolling
- Walking away or fitting in

None of these are small. They are bricks. Laid one by one, they quietly construct your character. You do not wake up strong or broken - you arrive there through repetition.

Pressure Is Loud, Values Are Quiet

The world rarely asks who you want to become. Instead, it pressures you with sameness: Why aren't you doing what everyone else is doing? Peer pressure does not always shove - sometimes it whispers.
$chunk$, 234),
        (8, $chunk$
"Come on, just try it."
"Don't be boring."
"Everyone else is doing it."

I met those voices in high school. Some pushed drugs. Some pushed parties. Some pushed lifestyles that did not match the future I wanted. I chose differently - not because I was better, but because I had direction. Values. Vision. Some of those kids paid dearly for their choices.

Choosing your future can feel lonely - but loneliness is cheaper than regret. Surround yourself with people who support your growth, not your excuses.

You Are Not Weak for Feeling Torn

If you ever feel confused, tempted, afraid, or unsure, it does not mean you are failing. It means you are human. The strongest people do not avoid difficult choices - they face them honestly. They admit when something feels good but leads somewhere harmful. They understand that short-term pleasure often demands long-term payment.

You are allowed to pause. You are allowed to think. You are allowed to choose what aligns with who you want to become - not who you are expected to be.

Your Tool for Difficult Decisions

Before you decide, pause and ask:

- Does this move me closer to who I want to become?
- Will I be proud of this tomorrow?
- Am I choosing comfort or growth?
$chunk$, 205),
        (9, $chunk$
You do not need to be fearless - only intentional. Courage is not the absence of fear; it is movement in spite of it.

Important decisions often hide inside ordinary moments. That is why I wish - deeply - that I had possessed a book like this when I was young: a place to return to, a steady framework for thought, a reminder that character is built choice by choice.

The good news is this: it is never too late to begin. Even now, as you read these words, you are choosing. You can skim and move on, or you can engage - reflect, underline, apply. Both are choices. Both shape consequences. Your future self is watching.

Mini Exercise

Grab a piece of paper or the margin of this book. Write one choice you are facing right now - big or small.

Then ask:

- What outcome do I truly want?
- Which option brings me closer to that life?
- What would the brave version of me do?
- If I were my future self, what would I choose today?

Make decisions like someone who loves their future.
$chunk$, 178),
        (10, $chunk$
Final Whistle - Choose With Love for Yourself

- I am not asking you to be perfect.
- I am asking you to be intentional.
- Love yourself enough to choose growth over comfort.
- Love yourself enough to walk away from what diminishes you.
- Love yourself enough to protect the future you are building.

One day, years from now, you will thank the version of yourself who chose wisely when it was hard - when no one was watching, when the reward was invisible, when courage felt lonely.

That version of you is being shaped right now, one decision at a time. Every choice writes a sentence in your life story. Make it one worth reading.
$chunk$, 105)
) AS chunks(chunk_index, content, token_count)
ON CONFLICT (book_id, chunk_index) DO UPDATE
SET content = EXCLUDED.content,
    token_count = EXCLUDED.token_count;
