-- ============================================================
-- LIBRARY DB SEED SCRIPT
-- Run: psql -h localhost -U postgres -d YOUR_DB -f seed.sql
-- ============================================================

-- Branch
INSERT INTO branches (name, location, department, is_active, max_borrow_days, fine_per_day) VALUES
('Main Library', 'Ground Floor, Main Building', 'General', true, 14, 10.00);

-- Books
INSERT INTO books (title, author, isbn, publisher, year, category, subject, description, cover_image_url, language, available, is_featured, featured_order) VALUES
('Clean Code', 'Robert C. Martin', '978-0132350884', 'Prentice Hall', 2008, 'Computer Science', 'Software Engineering', 'A handbook of agile software craftsmanship.', 'https://m.media-amazon.com/images/I/41xShlnTZTL._SX376_BO1,204,203,200_.jpg', 'English', true, true, 1),
('The Pragmatic Programmer', 'David Thomas', '978-0135957059', 'Addison-Wesley', 2019, 'Computer Science', 'Software Engineering', 'Your journey to mastery.', 'https://covers.openlibrary.org/b/isbn/9780201633610-L.jpg', 'English', true, true, 2),
('Introduction to Algorithms', 'Thomas H. Cormen', '978-0262033848', 'MIT Press', 2009, 'Computer Science', 'Algorithms', 'The comprehensive guide to algorithms.', 'https://covers.openlibrary.org/b/isbn/9780262033848-L.jpg', 'English', true, false, 0),
('Design Patterns', 'Gang of Four', '978-0201633610', 'Addison-Wesley', 1994, 'Computer Science', 'Software Design', 'Elements of reusable object-oriented software.', 'https://covers.openlibrary.org/b/isbn/9780201633610-L.jpg', 'English', true, false, 0),
('You Don''t Know JS', 'Kyle Simpson', '978-1491924464', 'O''Reilly Media', 2015, 'Computer Science', 'JavaScript', 'Deep dive into the core mechanisms of JavaScript.', 'https://covers.openlibrary.org/b/isbn/9781491924464-L.jpg', 'English', true, false, 0),
('Calculus: Early Transcendentals', 'James Stewart', '978-1285741550', 'Cengage Learning', 2015, 'Mathematics', 'Calculus', 'Comprehensive calculus textbook.', 'https://covers.openlibrary.org/b/isbn/9781285741550-L.jpg', 'English', true, false, 0),
('Linear Algebra and Its Applications', 'Gilbert Strang', '978-0030105678', 'Harcourt', 2005, 'Mathematics', 'Linear Algebra', 'Classic linear algebra text.', 'https://covers.openlibrary.org/b/isbn/9780030105678-L.jpg', 'English', true, false, 0),
('Discrete Mathematics', 'Kenneth H. Rosen', '978-0073383095', 'McGraw-Hill', 2011, 'Mathematics', 'Discrete Math', 'Foundations of discrete mathematics.', 'https://covers.openlibrary.org/b/isbn/9780073383095-L.jpg', 'English', true, false, 0),
('University Physics', 'Hugh D. Young', '978-0321973610', 'Pearson', 2015, 'Physics', 'General Physics', 'Standard university physics textbook.', 'https://covers.openlibrary.org/b/isbn/9780321973610-L.jpg', 'English', true, false, 0),
('A Brief History of Time', 'Stephen Hawking', '978-0553380163', 'Bantam Books', 1998, 'Physics', 'Cosmology', 'From the Big Bang to black holes.', 'https://covers.openlibrary.org/b/isbn/9780553380163-L.jpg', 'English', true, true, 3),
('Feynman Lectures on Physics', 'Richard P. Feynman', '978-0465023820', 'Basic Books', 2011, 'Physics', 'General Physics', 'The definitive physics lectures.', 'https://covers.openlibrary.org/b/isbn/9780465023820-L.jpg', 'English', true, false, 0),
('To Kill a Mockingbird', 'Harper Lee', '978-0061935466', 'HarperCollins', 2002, 'Literature', 'Fiction', 'A classic of modern American literature.', 'https://covers.openlibrary.org/b/isbn/9780061935466-L.jpg', 'English', true, true, 4),
('1984', 'George Orwell', '978-0451524935', 'Signet Classic', 1961, 'Literature', 'Dystopian Fiction', 'A dystopian social science fiction novel.', 'https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg', 'English', true, true, 5),
('The Great Gatsby', 'F. Scott Fitzgerald', '978-0743273565', 'Scribner', 2004, 'Literature', 'Fiction', 'The story of the mysteriously wealthy Jay Gatsby.', 'https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg', 'English', true, false, 0),
('Pride and Prejudice', 'Jane Austen', '978-0141439518', 'Penguin Classics', 2002, 'Literature', 'Classic Fiction', 'A romantic novel of manners.', 'https://covers.openlibrary.org/b/isbn/9780141439518-L.jpg', 'English', true, false, 0),
('Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', '978-0062316097', 'Harper', 2015, 'History', 'World History', 'A brief history of humankind.', 'https://covers.openlibrary.org/b/isbn/9780062316097-L.jpg', 'English', true, true, 6),
('Guns, Germs, and Steel', 'Jared Diamond', '978-0393317558', 'W. W. Norton', 1999, 'History', 'World History', 'The fates of human societies.', 'https://covers.openlibrary.org/b/isbn/9780393317558-L.jpg', 'English', true, false, 0),
('The Silk Roads', 'Peter Frankopan', '978-1101912379', 'Vintage', 2017, 'History', 'World History', 'A new history of the world.', 'https://covers.openlibrary.org/b/isbn/9781101912379-L.jpg', 'English', true, false, 0),
('Campbell Biology', 'Jane B. Reece', '978-0321775658', 'Pearson', 2011, 'Biology', 'General Biology', 'The gold standard in biology education.', 'https://covers.openlibrary.org/b/isbn/9780321775658-L.jpg', 'English', true, false, 0),
('The Selfish Gene', 'Richard Dawkins', '978-0198788607', 'Oxford University Press', 2016, 'Biology', 'Evolutionary Biology', 'A gene-centred view of evolution.', 'https://covers.openlibrary.org/b/isbn/9780198788607-L.jpg', 'English', true, false, 0),
('The Wealth of Nations', 'Adam Smith', '978-0140432084', 'Penguin Classics', 1999, 'Economics', 'Classical Economics', 'The foundation of modern economics.', 'https://covers.openlibrary.org/b/isbn/9780140432084-L.jpg', 'English', true, false, 0),
('Thinking, Fast and Slow', 'Daniel Kahneman', '978-0374533557', 'Farrar, Straus and Giroux', 2013, 'Economics', 'Behavioral Economics', 'How we think and make decisions.', 'https://covers.openlibrary.org/b/isbn/9780374533557-L.jpg', 'English', true, false, 0),
('Gray''s Anatomy', 'Henry Gray', '978-0702052309', 'Elsevier', 2015, 'Medicine', 'Anatomy', 'The definitive anatomy reference.', 'https://covers.openlibrary.org/b/isbn/9780702052309-L.jpg', 'English', true, false, 0),
('Harrison''s Principles of Internal Medicine', 'Dennis Kasper', '978-0071802154', 'McGraw-Hill', 2015, 'Medicine', 'Internal Medicine', 'The gold standard in internal medicine.', 'https://covers.openlibrary.org/b/isbn/9780071802154-L.jpg', 'English', true, false, 0),
('Fundamentals of Electric Circuits', 'Charles K. Alexander', '978-0078028229', 'McGraw-Hill', 2012, 'Engineering', 'Electrical Engineering', 'Core electrical circuits textbook.', 'https://covers.openlibrary.org/b/isbn/9780078028229-L.jpg', 'English', true, false, 0),
('Engineering Mechanics: Statics', 'Russell C. Hibbeler', '978-0133918922', 'Pearson', 2015, 'Engineering', 'Mechanical Engineering', 'Statics for engineering students.', 'https://covers.openlibrary.org/b/isbn/9780133918922-L.jpg', 'English', true, false, 0),
('Meditations', 'Marcus Aurelius', '978-0140449334', 'Penguin Classics', 180, 'Philosophy', 'Stoicism', 'Personal writings of Roman Emperor Marcus Aurelius.', 'https://covers.openlibrary.org/b/isbn/9780140449334-L.jpg', 'English', true, false, 0),
('The Republic', 'Plato', '978-0140455113', 'Penguin Classics', 2007, 'Philosophy', 'Political Philosophy', 'Plato explores justice and the ideal state.', 'https://covers.openlibrary.org/b/isbn/9780140455113-L.jpg', 'English', true, false, 0),
('Beyond Good and Evil', 'Friedrich Nietzsche', '978-0140449235', 'Penguin Classics', 2003, 'Philosophy', 'Ethics', 'Nietzsche critiques past philosophers.', 'https://covers.openlibrary.org/b/isbn/9780140449235-L.jpg', 'English', true, false, 0),
('The Story of Art', 'E.H. Gombrich', '978-0714832470', 'Phaidon Press', 2006, 'Art', 'Art History', 'One of the most famous books on art ever written.', 'https://covers.openlibrary.org/b/isbn/9780714832470-L.jpg', 'English', true, false, 0),
('Ways of Seeing', 'John Berger', '978-0140135152', 'Penguin Books', 1972, 'Art', 'Visual Arts', 'Influential work on how we look at art.', 'https://covers.openlibrary.org/b/isbn/9780140135152-L.jpg', 'English', true, false, 0),
('The Innovators', 'Walter Isaacson', '978-1476708706', 'Simon & Schuster', 2014, 'Technology', 'Innovation', 'How hackers and geeks created the digital revolution.', 'https://covers.openlibrary.org/b/isbn/9781476708706-L.jpg', 'English', true, false, 0),
('Zero to One', 'Peter Thiel', '978-0804139021', 'Crown Business', 2014, 'Technology', 'Startups', 'Notes on startups, or how to build the future.', 'https://covers.openlibrary.org/b/isbn/9780804139021-L.jpg', 'English', true, false, 0);

-- Inventory (5 copies each at Main Library)
INSERT INTO book_inventory (book_id, branch_id, total_copies, available_copies, shelf_location, condition)
SELECT b.id, 1, 5, 5,
  CASE b.category
    WHEN 'Computer Science' THEN 'CS-' || ROW_NUMBER() OVER (PARTITION BY b.category ORDER BY b.id)
    WHEN 'Mathematics'      THEN 'MA-' || ROW_NUMBER() OVER (PARTITION BY b.category ORDER BY b.id)
    WHEN 'Physics'          THEN 'PH-' || ROW_NUMBER() OVER (PARTITION BY b.category ORDER BY b.id)
    WHEN 'Literature'       THEN 'LT-' || ROW_NUMBER() OVER (PARTITION BY b.category ORDER BY b.id)
    WHEN 'History'          THEN 'HI-' || ROW_NUMBER() OVER (PARTITION BY b.category ORDER BY b.id)
    WHEN 'Biology'          THEN 'BI-' || ROW_NUMBER() OVER (PARTITION BY b.category ORDER BY b.id)
    WHEN 'Economics'        THEN 'EC-' || ROW_NUMBER() OVER (PARTITION BY b.category ORDER BY b.id)
    WHEN 'Medicine'         THEN 'ME-' || ROW_NUMBER() OVER (PARTITION BY b.category ORDER BY b.id)
    WHEN 'Engineering'      THEN 'EN-' || ROW_NUMBER() OVER (PARTITION BY b.category ORDER BY b.id)
    WHEN 'Philosophy'       THEN 'PL-' || ROW_NUMBER() OVER (PARTITION BY b.category ORDER BY b.id)
    WHEN 'Art'              THEN 'AR-' || ROW_NUMBER() OVER (PARTITION BY b.category ORDER BY b.id)
    WHEN 'Technology'       THEN 'TC-' || ROW_NUMBER() OVER (PARTITION BY b.category ORDER BY b.id)
    ELSE 'GN-' || ROW_NUMBER() OVER (PARTITION BY b.category ORDER BY b.id)
  END,
  'GOOD'
FROM books b;

-- Admin user (password: Admin@123)
INSERT INTO users (full_name, email, password, role, is_active, is_email_verified, created_at)
VALUES (
  'Admin User',
  'admin@library.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lihO',
  'SUPER_ADMIN',
  true,
  true,
  NOW()
);
