-- ============================================================
-- Library Management System - Full Seed
-- Run AFTER app starts (tables auto-created by Hibernate)
-- ============================================================

-- ============================================================
-- CLEAN SLATE
-- ============================================================
TRUNCATE TABLE book_inventory, book_reviews, borrow_transactions,
               reservations, notifications, fines,
               books, branches
RESTART IDENTITY CASCADE;

-- ============================================================
-- ADMIN USER
-- Email: admin@library.com | Password: password123
-- ============================================================
INSERT INTO users (full_name, email, password, role, is_active, is_email_verified, created_at, updated_at)
VALUES (
  'Super Admin',
  'admin@library.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lihO',
  'SUPER_ADMIN',
  true, true,
  NOW(), NOW()
);

-- ============================================================
-- BRANCH
-- ============================================================
INSERT INTO branches (name, department, location, phone, email, operating_hours, max_borrow_days, fine_per_day, is_active, created_at)
VALUES ('Main Library', 'Central', 'Ground Floor, Main Building', '9000000001', 'library@college.edu', 'Mon-Sat 8am-8pm', 14, 2.00, true, NOW());

-- ============================================================
-- BOOKS
-- ============================================================
INSERT INTO books (title, author, isbn, publisher, edition, year, category, subject, description, cover_image_url, language, total_pages, available, is_featured, featured_order, created_at)
VALUES
-- Computer Science
('Clean Code', 'Robert C. Martin', '978-0132350884', 'Prentice Hall', '1st', 2008, 'Computer Science', 'Software Engineering', 'A handbook of agile software craftsmanship covering clean, readable, and maintainable code.', 'https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg', 'English', 431, true, true, 1, NOW()),
('The Pragmatic Programmer', 'David Thomas', '978-0135957059', 'Addison-Wesley', '2nd', 2019, 'Computer Science', 'Software Engineering', 'Your journey to mastery. Practical approaches to software development.', 'https://covers.openlibrary.org/b/isbn/9780135957059-L.jpg', 'English', 352, true, true, 2, NOW()),
('Introduction to Algorithms', 'Thomas H. Cormen', '978-0262033848', 'MIT Press', '3rd', 2009, 'Computer Science', 'Algorithms', 'Comprehensive introduction to algorithms and data structures.', 'https://covers.openlibrary.org/b/isbn/9780262033848-L.jpg', 'English', 1292, true, true, 3, NOW()),
('Design Patterns', 'Gang of Four', '978-0201633610', 'Addison-Wesley', '1st', 1994, 'Computer Science', 'Software Design', 'Elements of reusable object-oriented software. The classic patterns book.', 'https://covers.openlibrary.org/b/isbn/9780201633610-L.jpg', 'English', 395, true, true, 4, NOW()),
('You Don''t Know JS', 'Kyle Simpson', '978-1491924464', 'O''Reilly Media', '1st', 2015, 'Computer Science', 'JavaScript', 'Deep dive into the core mechanisms of the JavaScript language.', 'https://covers.openlibrary.org/b/isbn/9781491924464-L.jpg', 'English', 278, true, true, 5, NOW()),

-- Mathematics
('Calculus: Early Transcendentals', 'James Stewart', '978-1285741550', 'Cengage Learning', '8th', 2015, 'Mathematics', 'Calculus', 'Comprehensive calculus textbook covering single and multivariable calculus.', 'https://covers.openlibrary.org/b/isbn/9781285741550-L.jpg', 'English', 1368, true, false, 0, NOW()),
('Linear Algebra and Its Applications', 'Gilbert Strang', '978-0030105678', 'Harcourt', '3rd', 2006, 'Mathematics', 'Linear Algebra', 'Accessible introduction to linear algebra with applications.', 'https://covers.openlibrary.org/b/isbn/9780030105678-L.jpg', 'English', 544, true, false, 0, NOW()),
('Discrete Mathematics', 'Kenneth H. Rosen', '978-0073383095', 'McGraw-Hill', '7th', 2011, 'Mathematics', 'Discrete Math', 'Covers logic, sets, relations, graphs, and combinatorics.', 'https://covers.openlibrary.org/b/isbn/9780073383095-L.jpg', 'English', 976, true, false, 0, NOW()),

-- Physics
('University Physics', 'Hugh D. Young', '978-0321973610', 'Pearson', '14th', 2015, 'Physics', 'General Physics', 'Comprehensive university-level physics textbook with calculus.', 'https://covers.openlibrary.org/b/isbn/9780321973610-L.jpg', 'English', 1600, true, false, 0, NOW()),
('A Brief History of Time', 'Stephen Hawking', '978-0553380163', 'Bantam Books', '1st', 1998, 'Physics', 'Cosmology', 'From the Big Bang to black holes. A landmark volume in science writing.', 'https://covers.openlibrary.org/b/isbn/9780553380163-L.jpg', 'English', 212, true, false, 0, NOW()),
('Feynman Lectures on Physics', 'Richard P. Feynman', '978-0465023820', 'Basic Books', 'New Millennium', 2011, 'Physics', 'General Physics', 'The definitive edition of Feynman''s legendary lectures.', 'https://covers.openlibrary.org/b/isbn/9780465023820-L.jpg', 'English', 1552, true, false, 0, NOW()),

-- Literature
('To Kill a Mockingbird', 'Harper Lee', '978-0061935466', 'HarperCollins', 'Perennial', 2002, 'Literature', 'Fiction', 'A gripping tale of racial injustice and childhood innocence in the American South.', 'https://covers.openlibrary.org/b/isbn/9780061935466-L.jpg', 'English', 336, true, false, 0, NOW()),
('1984', 'George Orwell', '978-0451524935', 'Signet Classic', 'Reissue', 1961, 'Literature', 'Dystopian Fiction', 'A chilling prophecy about the future. A masterpiece of political fiction.', 'https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg', 'English', 328, true, false, 0, NOW()),
('The Great Gatsby', 'F. Scott Fitzgerald', '978-0743273565', 'Scribner', 'Reissue', 2004, 'Literature', 'Fiction', 'A story of the fabulously wealthy Jay Gatsby and his love for Daisy Buchanan.', 'https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg', 'English', 180, true, false, 0, NOW()),
('Pride and Prejudice', 'Jane Austen', '978-0141439518', 'Penguin Classics', 'Revised', 2002, 'Literature', 'Classic Fiction', 'A romantic novel of manners set in rural England at the turn of the 19th century.', 'https://covers.openlibrary.org/b/isbn/9780141439518-L.jpg', 'English', 480, true, false, 0, NOW()),

-- History
('Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', '978-0062316097', 'Harper', '1st', 2015, 'History', 'World History', 'A bold exploration of the history and impact of Homo sapiens.', 'https://covers.openlibrary.org/b/isbn/9780062316097-L.jpg', 'English', 443, true, false, 0, NOW()),
('Guns, Germs, and Steel', 'Jared Diamond', '978-0393317558', 'W. W. Norton', 'Reissue', 1999, 'History', 'World History', 'The fates of human societies. Why did some civilizations conquer others?', 'https://covers.openlibrary.org/b/isbn/9780393317558-L.jpg', 'English', 480, true, false, 0, NOW()),
('The Silk Roads', 'Peter Frankopan', '978-1101912379', 'Vintage', '1st', 2017, 'History', 'World History', 'A new history of the world through the lens of the Silk Roads.', 'https://covers.openlibrary.org/b/isbn/9781101912379-L.jpg', 'English', 672, true, false, 0, NOW()),

-- Biology
('Campbell Biology', 'Jane B. Reece', '978-0321775658', 'Pearson', '9th', 2011, 'Biology', 'General Biology', 'The gold standard in biology education. Comprehensive and authoritative.', 'https://covers.openlibrary.org/b/isbn/9780321775658-L.jpg', 'English', 1464, true, false, 0, NOW()),
('The Selfish Gene', 'Richard Dawkins', '978-0198788607', 'Oxford University Press', '40th Anniversary', 2016, 'Biology', 'Evolutionary Biology', 'A landmark work in evolutionary biology. Introduces the gene-centered view.', 'https://covers.openlibrary.org/b/isbn/9780198788607-L.jpg', 'English', 544, true, false, 0, NOW()),

-- Economics
('The Wealth of Nations', 'Adam Smith', '978-0140432084', 'Penguin Classics', 'Reprint', 1999, 'Economics', 'Classical Economics', 'The foundational work of classical economics and capitalism.', 'https://covers.openlibrary.org/b/isbn/9780140432084-L.jpg', 'English', 1264, true, false, 0, NOW()),
('Thinking, Fast and Slow', 'Daniel Kahneman', '978-0374533557', 'Farrar, Straus and Giroux', 'Reprint', 2013, 'Economics', 'Behavioral Economics', 'A groundbreaking tour of the mind explaining the two systems that drive the way we think.', 'https://covers.openlibrary.org/b/isbn/9780374533557-L.jpg', 'English', 499, true, false, 0, NOW()),

-- Medicine
('Gray''s Anatomy', 'Henry Gray', '978-0702052309', 'Elsevier', '41st', 2015, 'Medicine', 'Anatomy', 'The definitive human anatomy reference. Used by medical students worldwide.', 'https://covers.openlibrary.org/b/isbn/9780702052309-L.jpg', 'English', 1576, true, false, 0, NOW()),
('Harrison''s Principles of Internal Medicine', 'Dennis Kasper', '978-0071802154', 'McGraw-Hill', '19th', 2015, 'Medicine', 'Internal Medicine', 'The gold standard of internal medicine references.', 'https://covers.openlibrary.org/b/isbn/9780071802154-L.jpg', 'English', 3983, true, false, 0, NOW()),

-- Engineering
('Fundamentals of Electric Circuits', 'Charles K. Alexander', '978-0078028229', 'McGraw-Hill', '5th', 2012, 'Engineering', 'Electrical Engineering', 'A comprehensive introduction to electric circuit analysis.', 'https://covers.openlibrary.org/b/isbn/9780078028229-L.jpg', 'English', 960, true, false, 0, NOW()),
('Engineering Mechanics: Statics', 'Russell C. Hibbeler', '978-0133918922', 'Pearson', '14th', 2015, 'Engineering', 'Mechanical Engineering', 'Covers the principles of statics with applications to engineering problems.', 'https://covers.openlibrary.org/b/isbn/9780133918922-L.jpg', 'English', 672, true, false, 0, NOW());

-- ============================================================
-- BOOK INVENTORY (5 copies each at branch id=1)
-- ============================================================
INSERT INTO book_inventory (book_id, branch_id, total_copies, available_copies, shelf_location, condition, added_at)
SELECT
  b.id,
  1,
  5,
  5,
  CONCAT(
    CASE b.category
      WHEN 'Computer Science' THEN 'CS'
      WHEN 'Mathematics'      THEN 'MA'
      WHEN 'Physics'          THEN 'PH'
      WHEN 'Literature'       THEN 'LT'
      WHEN 'History'          THEN 'HI'
      WHEN 'Biology'          THEN 'BI'
      WHEN 'Economics'        THEN 'EC'
      WHEN 'Medicine'         THEN 'ME'
      WHEN 'Engineering'      THEN 'EN'
      ELSE 'GN'
    END,
    '-',
    ROW_NUMBER() OVER (PARTITION BY b.category ORDER BY b.id)
  ),
  'GOOD',
  NOW()
FROM books b;
